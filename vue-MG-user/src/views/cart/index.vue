<script setup>
import { computed, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';

import { useCartStore } from '@/stores/cart';
import { formatCurrency } from '@/utils/shop';

const router = useRouter();
const cartStore = useCartStore();

const selectedKeys = ref([]);

watch(
  () => cartStore.state.items,
  (items) => {
    const validKeys = items.map((item) => item.key);
    selectedKeys.value = selectedKeys.value.filter((key) => validKeys.includes(key));

    if (!selectedKeys.value.length && validKeys.length) {
      selectedKeys.value = [...validKeys];
    }
  },
  { immediate: true, deep: true }
);

const allSelected = computed({
  get: () =>
    !!cartStore.state.items.length &&
    selectedKeys.value.length === cartStore.state.items.length,
  set: (value) => {
    selectedKeys.value = value ? cartStore.state.items.map((item) => item.key) : [];
  }
});

const selectedItems = computed(() =>
  cartStore.state.items.filter((item) => selectedKeys.value.includes(item.key))
);

const selectedAmount = computed(() =>
  selectedItems.value.reduce((total, item) => total + item.price * item.quantity, 0)
);

const selectedCount = computed(() =>
  selectedItems.value.reduce((total, item) => total + item.quantity, 0)
);

const toggleSelected = (key, checked) => {
  if (checked) {
    if (!selectedKeys.value.includes(key)) {
      selectedKeys.value = [...selectedKeys.value, key];
    }
    return;
  }

  selectedKeys.value = selectedKeys.value.filter((itemKey) => itemKey !== key);
};

const handleCheckout = () => {
  if (!selectedItems.value.length) {
    ElMessage.warning('请先勾选要结算的商品');
    return;
  }

  ElMessage.success('购物车结算流程已预留，后续接入下单接口即可继续开发');
};

const handleClear = () => {
  ElMessageBox.confirm('确认清空购物车吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    cartStore.clear();
    selectedKeys.value = [];
  }).catch(() => {});
};
</script>

<template>
  <div class="cart-page">
    <section class="cart-page__hero">
      <div>
        <p class="cart-page__eyebrow">Shopping Cart</p>
        <h2>购物车</h2>
        <span>已为后续“确认订单 / 支付 / 下单”流程预留前端结构。</span>
      </div>

      <div class="cart-page__hero-actions">
        <el-button @click="router.push('/user/home')">继续选购</el-button>
        <el-button type="danger" plain :disabled="!cartStore.state.items.length" @click="handleClear">
          清空购物车
        </el-button>
      </div>
    </section>

    <div v-if="cartStore.state.items.length" class="cart-page__grid">
      <section class="cart-list">
        <div class="cart-list__toolbar">
          <el-checkbox v-model="allSelected">全选</el-checkbox>
          <span>共 {{ cartStore.state.items.length }} 条购物记录</span>
        </div>

        <article v-for="item in cartStore.state.items" :key="item.key" class="cart-item">
          <el-checkbox
            :model-value="selectedKeys.includes(item.key)"
            @change="(checked) => toggleSelected(item.key, checked)"
          />

          <img :src="item.image" :alt="item.name" class="cart-item__image" />

          <div class="cart-item__body">
            <div class="cart-item__title-row">
              <h3>{{ item.name }}</h3>
              <span>{{ item.type === 'setmeal' ? '套餐' : '补剂' }}</span>
            </div>
            <p>{{ item.description || '已加入购物车，等待结算。' }}</p>
            <small v-if="item.specText">{{ item.specText }}</small>
          </div>

          <div class="cart-item__price">
            <strong>¥{{ formatCurrency(item.price) }}</strong>
            <span>小计 ¥{{ formatCurrency(item.price * item.quantity) }}</span>
          </div>

          <div class="cart-item__stepper">
            <el-button circle plain @click="cartStore.decrease(item.key)">-</el-button>
            <span>{{ item.quantity }}</span>
            <el-button circle plain @click="cartStore.increase(item.key)">+</el-button>
          </div>

          <el-button link type="danger" @click="cartStore.remove(item.key)">删除</el-button>
        </article>
      </section>

      <aside class="cart-summary">
        <p class="cart-summary__eyebrow">Order Preview</p>
        <h3>结算清单</h3>

        <div class="cart-summary__row">
          <span>已选商品数</span>
          <strong>{{ selectedCount }}</strong>
        </div>

        <div class="cart-summary__row">
          <span>购物车总数</span>
          <strong>{{ cartStore.totalCount.value }}</strong>
        </div>

        <div class="cart-summary__row cart-summary__row--amount">
          <span>待结算金额</span>
          <strong>¥{{ formatCurrency(selectedAmount) }}</strong>
        </div>

        <el-button class="cart-summary__submit" type="warning" @click="handleCheckout">
          选择结算下单
        </el-button>
      </aside>
    </div>

    <section v-else class="cart-empty">
      <el-empty description="购物车还是空的，去首页挑几件补剂吧" />
      <el-button type="warning" @click="router.push('/user/home')">去首页看看</el-button>
    </section>
  </div>
</template>

<style scoped>
.cart-page {
  animation: fadeUp 0.45s ease;
}

.cart-page__hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 28px;
  background: linear-gradient(145deg, rgba(255, 248, 238, 0.94), rgba(248, 232, 205, 0.76));
  border: 1px solid rgba(186, 145, 83, 0.14);
  border-radius: 28px;
}

.cart-page__eyebrow {
  margin: 0 0 8px;
  color: #b17b3f;
  font-size: 12px;
  letter-spacing: 0.22em;
  text-transform: uppercase;
}

.cart-page__hero h2 {
  margin: 0 0 10px;
  font-size: 34px;
  color: #321f12;
}

.cart-page__hero span {
  color: #7a5b3e;
}

.cart-page__hero-actions {
  display: flex;
  gap: 12px;
}

.cart-page__grid {
  display: grid;
  grid-template-columns: 1.3fr 0.78fr;
  gap: 20px;
  margin-top: 20px;
}

.cart-list,
.cart-summary,
.cart-empty {
  padding: 26px;
  background: rgba(255, 251, 244, 0.94);
  border: 1px solid rgba(186, 145, 83, 0.14);
  border-radius: 28px;
}

.cart-list__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
  color: #826245;
}

.cart-item {
  display: grid;
  grid-template-columns: auto 108px 1fr auto auto auto;
  gap: 16px;
  align-items: center;
  padding: 16px 0;
  border-top: 1px solid rgba(188, 153, 103, 0.12);
}

.cart-item:first-of-type {
  border-top: none;
}

.cart-item__image {
  width: 108px;
  height: 88px;
  object-fit: cover;
  border-radius: 20px;
  background: #fff0dc;
}

.cart-item__title-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.cart-item__title-row h3 {
  margin: 0;
  color: #311f12;
}

.cart-item__title-row span {
  padding: 4px 10px;
  border-radius: 999px;
  background: #fff1de;
  color: #bf7b21;
  font-size: 12px;
}

.cart-item__body p,
.cart-item__body small {
  display: block;
  margin: 8px 0 0;
  color: #826245;
  line-height: 1.7;
}

.cart-item__price {
  text-align: right;
}

.cart-item__price strong {
  display: block;
  color: #ff7d1c;
  font-size: 22px;
}

.cart-item__price span {
  color: #9b7650;
  font-size: 13px;
}

.cart-item__stepper {
  display: flex;
  align-items: center;
  gap: 10px;
}

.cart-item__stepper span {
  min-width: 24px;
  text-align: center;
  font-weight: 700;
}

.cart-summary__eyebrow {
  margin: 0 0 8px;
  color: #b17b3f;
  font-size: 12px;
  letter-spacing: 0.22em;
  text-transform: uppercase;
}

.cart-summary h3 {
  margin: 0 0 22px;
  font-size: 30px;
  color: #321f12;
}

.cart-summary__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  border-top: 1px solid rgba(188, 153, 103, 0.12);
  color: #725438;
}

.cart-summary__row--amount strong {
  font-size: 30px;
  color: #ff7d1c;
}

.cart-summary__submit {
  width: 100%;
  height: 52px;
  margin-top: 22px;
  border: none;
  border-radius: 18px;
  font-weight: 700;
}

.cart-empty {
  margin-top: 20px;
  display: grid;
  place-items: center;
  gap: 14px;
}

@media (max-width: 1360px) {
  .cart-page__grid {
    grid-template-columns: 1fr;
  }

  .cart-item {
    grid-template-columns: auto 88px 1fr;
  }

  .cart-item__price,
  .cart-item__stepper {
    justify-content: flex-start;
    text-align: left;
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
