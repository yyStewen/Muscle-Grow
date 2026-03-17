<script setup>
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';

import {
  getSetmealSupplements,
  getSetmealsByCategory,
  getSupplementsByCategory
} from '@/api/shop';
import CartDock from '@/components/shop/CartDock.vue';
import SetmealDrawer from '@/components/shop/SetmealDrawer.vue';
import SpecificationDialog from '@/components/shop/SpecificationDialog.vue';
import { useCatalogStore } from '@/stores/catalog';
import { useCartStore } from '@/stores/cart';
import { formatCurrency, normalizeSupplement } from '@/utils/shop';

const router = useRouter();
const route = useRoute();

const cartStore = useCartStore();
const catalogStore = useCatalogStore();

const loading = ref(false);
const products = ref([]);

const specDialogVisible = ref(false);
const activeSupplement = ref(null);

const setmealDrawerVisible = ref(false);
const activeSetmeal = ref(null);
const setmealLoading = ref(false);
const setmealItems = ref([]);

const currentKind = computed(() => (route.params.kind === 'setmeal' ? 'setmeal' : 'supplement'));
const currentCategoryId = computed(() => Number(route.params.categoryId || 0));

const currentCategoryName = computed(() => {
  const pool =
    currentKind.value === 'setmeal'
      ? catalogStore.state.setmealCategories
      : catalogStore.state.supplementCategories;

  return pool.find((item) => item.id === currentCategoryId.value)?.name || '精选好物';
});

const heroTitle = computed(() =>
  currentKind.value === 'setmeal' ? '套餐搭配专区' : '补剂精选货架'
);

const heroDescription = computed(() =>
  currentKind.value === 'setmeal'
    ? '面向增肌、减脂、恢复等不同目标提供组合套餐，一键配齐训练所需。'
    : '支持按分类浏览补剂，有规格信息时可先选择口味/容量/剂型，再加入购物车。'
);

const fetchProducts = async () => {
  if (!currentCategoryId.value) {
    products.value = [];
    return;
  }

  loading.value = true;

  try {
    const res =
      currentKind.value === 'setmeal'
        ? await getSetmealsByCategory(currentCategoryId.value)
        : await getSupplementsByCategory(currentCategoryId.value);

    if (res.code === 1) {
      products.value =
        currentKind.value === 'setmeal'
          ? res.data || []
          : (res.data || []).map((item) => normalizeSupplement(item));
    } else {
      products.value = [];
      ElMessage.error(res.msg || '商品加载失败');
    }
  } catch (error) {
    products.value = [];
  } finally {
    loading.value = false;
  }
};

watch([currentKind, currentCategoryId], fetchProducts, { immediate: true });

const addSupplementDirect = (product) => {
  cartStore.addSupplement(product);
  ElMessage.success('已加入购物车');
};

const handleSupplementAction = (product) => {
  if (product.hasSpecs) {
    activeSupplement.value = product;
    specDialogVisible.value = true;
    return;
  }

  addSupplementDirect(product);
};

const handleSpecConfirm = (selection) => {
  if (!activeSupplement.value) return;

  cartStore.addSupplement(activeSupplement.value, selection);
  ElMessage.success('已按所选规格加入购物车');
};

const openSetmealDrawer = async (setmeal) => {
  activeSetmeal.value = setmeal;
  setmealDrawerVisible.value = true;
  setmealLoading.value = true;

  try {
    const res = await getSetmealSupplements(setmeal.id);
    setmealItems.value = res.code === 1 ? res.data || [] : [];
  } finally {
    setmealLoading.value = false;
  }
};

const addSetmealToCart = (setmeal) => {
  cartStore.addSetmeal(setmeal);
  ElMessage.success('套餐已加入购物车');
};

const handleCheckout = () => {
  router.push('/user/cart');
};
</script>

<template>
  <div class="shop-home">
    <section class="shop-hero">
      <div>
        <p class="shop-hero__eyebrow">{{ currentCategoryName }}</p>
        <h2>{{ heroTitle }}</h2>
        <p class="shop-hero__desc">{{ heroDescription }}</p>

        <div class="shop-hero__meta">
          <span>当前分类 {{ currentCategoryName }}</span>
          <span>{{ products.length }} 件可选商品</span>
          <span>支持补剂规格选择 / 套餐补剂预览</span>
        </div>
      </div>

      <div class="shop-hero__card">
        <p>营养补给站</p>
        <strong>训练日前后 30 分钟补给效率最高</strong>
        <span>根据你的训练节奏，把补剂、套餐和购物车选择整合到一套桌面端流程里。</span>
      </div>
    </section>

    <section class="product-board" v-loading="loading">
      <div class="product-board__header">
        <div>
          <p>{{ currentKind === 'setmeal' ? '套餐列表' : '补剂列表' }}</p>
          <h3>{{ currentCategoryName }}</h3>
        </div>

        <el-button class="product-board__cart-link" @click="router.push('/user/cart')">
          查看购物车
        </el-button>
      </div>

      <div v-if="products.length" class="product-list">
        <article
          v-for="item in products"
          :key="`${currentKind}-${item.id}`"
          class="product-item"
        >
          <img :src="item.image" :alt="item.name" class="product-item__image" />

          <div class="product-item__body">
            <div class="product-item__title-row">
              <h4>{{ item.name }}</h4>
              <span v-if="currentKind === 'supplement' && item.hasSpecs" class="product-item__tag">
                多规格可选
              </span>
              <span v-if="currentKind === 'setmeal'" class="product-item__tag product-item__tag--setmeal">
                组合搭配
              </span>
            </div>

            <p class="product-item__desc">
              {{ item.description || (currentKind === 'setmeal' ? '组合补给，适合一键选购。' : '专业训练补给方案。') }}
            </p>

            <div
              v-if="currentKind === 'supplement' && item.detailGroups?.length"
              class="product-item__chips"
            >
              <span v-for="detail in item.detailGroups.slice(0, 2)" :key="detail.id || detail.name">
                {{ detail.name }} · {{ detail.values.slice(0, 2).join(' / ') }}
              </span>
            </div>

            <div v-if="currentKind === 'setmeal'" class="product-item__inline-actions">
              <el-button link type="warning" @click="openSetmealDrawer(item)">查看套餐内容</el-button>
            </div>

            <div class="product-item__price-row">
              <strong>¥{{ formatCurrency(item.price) }}</strong>
              <span>{{ currentKind === 'setmeal' ? '推荐目标型组合' : '支持快速补货' }}</span>
            </div>
          </div>

          <div class="product-item__action">
            <el-button
              v-if="currentKind === 'supplement' && item.hasSpecs"
              round
              type="warning"
              @click="handleSupplementAction(item)"
            >
              选择规格
            </el-button>

            <el-button
              v-else-if="currentKind === 'supplement'"
              circle
              type="warning"
              class="product-item__circle"
              @click="handleSupplementAction(item)"
            >
              <el-icon><Plus /></el-icon>
            </el-button>

            <div v-else class="product-item__setmeal-actions">
              <el-button round plain type="warning" @click="openSetmealDrawer(item)">
                套餐详情
              </el-button>
              <el-button circle type="warning" class="product-item__circle" @click="addSetmealToCart(item)">
                <el-icon><Plus /></el-icon>
              </el-button>
            </div>
          </div>
        </article>
      </div>

      <el-empty v-else description="该分类下暂时还没有上架商品" />
    </section>

    <CartDock
      :total-amount="cartStore.totalAmount.value"
      :total-count="cartStore.totalCount.value"
      @open-cart="router.push('/user/cart')"
      @checkout="handleCheckout"
    />

    <SpecificationDialog
      v-model="specDialogVisible"
      :product="activeSupplement"
      @confirm="handleSpecConfirm"
    />

    <SetmealDrawer
      v-model="setmealDrawerVisible"
      :setmeal="activeSetmeal"
      :items="setmealItems"
      :loading="setmealLoading"
      @add="activeSetmeal && addSetmealToCart(activeSetmeal)"
    />
  </div>
</template>

<style scoped>
.shop-home {
  animation: fadeUp 0.45s ease;
}

.shop-hero {
  display: grid;
  grid-template-columns: 1.3fr 0.9fr;
  gap: 20px;
  padding: 28px;
  background: linear-gradient(145deg, rgba(255, 249, 240, 0.88), rgba(244, 226, 198, 0.72));
  border: 1px solid rgba(191, 143, 77, 0.16);
  border-radius: 30px;
  box-shadow: 0 18px 48px rgba(102, 67, 22, 0.08);
}

.shop-hero__eyebrow {
  margin: 0 0 10px;
  font-size: 12px;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  color: #a67848;
}

.shop-hero h2 {
  margin: 0 0 14px;
  font-size: 34px;
  color: #311f11;
}

.shop-hero__desc {
  margin: 0;
  max-width: 720px;
  font-size: 16px;
  line-height: 1.8;
  color: #73553c;
}

.shop-hero__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 18px;
}

.shop-hero__meta span {
  padding: 9px 14px;
  border-radius: 999px;
  background: #fff6ea;
  color: #835f3d;
  font-size: 13px;
}

.shop-hero__card {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 24px;
  background:
    radial-gradient(circle at top right, rgba(255, 216, 104, 0.42), transparent 34%),
    linear-gradient(135deg, #3c220f, #7b481d);
  border-radius: 28px;
  color: #fff7ea;
}

.shop-hero__card p {
  margin: 0 0 10px;
  color: rgba(255, 247, 234, 0.72);
  letter-spacing: 0.18em;
  text-transform: uppercase;
  font-size: 12px;
}

.shop-hero__card strong {
  font-size: 24px;
  line-height: 1.4;
}

.shop-hero__card span {
  margin-top: 12px;
  line-height: 1.8;
  color: rgba(255, 247, 234, 0.74);
}

.product-board {
  margin-top: 20px;
  padding: 28px;
  background: rgba(255, 251, 244, 0.92);
  border: 1px solid rgba(181, 145, 90, 0.14);
  border-radius: 30px;
}

.product-board__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 20px;
}

.product-board__header p {
  margin: 0 0 6px;
  color: #aa7b47;
  font-size: 12px;
  letter-spacing: 0.22em;
  text-transform: uppercase;
}

.product-board__header h3 {
  margin: 0;
  font-size: 30px;
  color: #2d1c10;
}

.product-board__cart-link {
  height: 46px;
  border: none;
  border-radius: 16px;
  background: #2f2118;
  color: #fff6ea;
}

.product-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.product-item {
  display: grid;
  grid-template-columns: 164px 1fr auto;
  gap: 18px;
  align-items: center;
  padding: 20px;
  background: #ffffff;
  border-radius: 26px;
  box-shadow: 0 10px 30px rgba(113, 76, 31, 0.08);
  transition: transform 0.24s ease, box-shadow 0.24s ease;
}

.product-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 18px 36px rgba(113, 76, 31, 0.14);
}

.product-item__image {
  width: 164px;
  height: 132px;
  object-fit: cover;
  border-radius: 24px;
  background: #fff1dc;
}

.product-item__title-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-item__title-row h4 {
  margin: 0;
  font-size: 28px;
  color: #301d10;
}

.product-item__tag {
  padding: 6px 12px;
  border-radius: 999px;
  background: #fff4db;
  color: #b7740f;
  font-size: 12px;
  font-weight: 700;
}

.product-item__tag--setmeal {
  background: #fff0e4;
  color: #cf6c1b;
}

.product-item__desc {
  margin: 10px 0 0;
  color: #7a5a3e;
  line-height: 1.8;
}

.product-item__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 14px;
}

.product-item__chips span {
  padding: 8px 12px;
  border-radius: 999px;
  background: #fff8ee;
  color: #8a6846;
  font-size: 13px;
}

.product-item__inline-actions {
  margin-top: 14px;
}

.product-item__price-row {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-top: 18px;
}

.product-item__price-row strong {
  font-size: 34px;
  color: #ff7c1f;
}

.product-item__price-row span {
  color: #a17b52;
}

.product-item__action {
  display: flex;
  align-items: center;
}

.product-item__circle {
  width: 54px;
  height: 54px;
  border: none;
}

.product-item__setmeal-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

@media (max-width: 1320px) {
  .shop-hero,
  .product-item {
    grid-template-columns: 1fr;
  }

  .product-item__image {
    width: 100%;
    height: 220px;
  }

  .product-item__action {
    justify-content: flex-end;
  }
}

@keyframes fadeUp {
  from {
    opacity: 0;
    transform: translateY(12px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
