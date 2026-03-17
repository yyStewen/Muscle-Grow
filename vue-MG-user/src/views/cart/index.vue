<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';

import { useCartStore } from '@/stores/cart';
import { formatCurrency } from '@/utils/shop';

const router = useRouter();
const cartStore = useCartStore();

const selectedKeys = ref([]);

onMounted(async () => {
  await cartStore.fetchCart();
});

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
    ElMessage.warning('Please select items before checkout.');
    return;
  }

  ElMessage.success('Checkout flow is reserved for the next step.');
};

const handleClear = () => {
  ElMessageBox.confirm('Clear all items from the cart?', 'Confirm', {
    confirmButtonText: 'Clear',
    cancelButtonText: 'Cancel',
    type: 'warning'
  }).then(async () => {
    const success = await cartStore.clear();
    if (success) {
      selectedKeys.value = [];
      ElMessage.success('Cart cleared');
    }
  }).catch(() => {});
};

const handleIncrease = async (item) => {
  await cartStore.increase(item);
};

const handleDecrease = async (item) => {
  await cartStore.decrease(item);
};

const handleRemove = async (item) => {
  const success = await cartStore.remove(item);
  if (success) {
    ElMessage.success('Item removed');
  }
};
</script>

<template>
  <div class="cart-page">
    <section class="cart-page__hero">
      <div>
        <p class="cart-page__eyebrow">Shopping Cart</p>
        <h2>Cart</h2>
        <span>The cart is now backed by the real backend list and clear APIs.</span>
      </div>

      <div class="cart-page__hero-actions">
        <el-button @click="router.push('/user/home')">Continue Shopping</el-button>
        <el-button
          type="danger"
          plain
          :disabled="!cartStore.state.items.length || cartStore.state.syncing"
          @click="handleClear"
        >
          Clear Cart
        </el-button>
      </div>
    </section>

    <div v-if="cartStore.state.items.length" class="cart-page__grid" v-loading="cartStore.state.loading">
      <section class="cart-list">
        <div class="cart-list__toolbar">
          <el-checkbox v-model="allSelected">Select All</el-checkbox>
          <span>{{ cartStore.state.items.length }} cart rows</span>
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
              <span>{{ item.type === 'setmeal' ? 'Setmeal' : 'Supplement' }}</span>
            </div>
            <p>{{ item.type === 'setmeal' ? 'Setmeal item in cart.' : 'Supplement item in cart.' }}</p>
            <small v-if="item.specText">{{ item.specText }}</small>
          </div>

          <div class="cart-item__price">
            <strong>¥{{ formatCurrency(item.price) }}</strong>
            <span>Subtotal ¥{{ formatCurrency(item.price * item.quantity) }}</span>
          </div>

          <div class="cart-item__stepper">
            <el-button circle plain :disabled="cartStore.state.syncing" @click="handleDecrease(item)">-</el-button>
            <span>{{ item.quantity }}</span>
            <el-button circle plain :disabled="cartStore.state.syncing" @click="handleIncrease(item)">+</el-button>
          </div>

          <el-button link type="danger" :disabled="cartStore.state.syncing" @click="handleRemove(item)">
            Remove
          </el-button>
        </article>
      </section>

      <aside class="cart-summary">
        <p class="cart-summary__eyebrow">Order Preview</p>
        <h3>Summary</h3>

        <div class="cart-summary__row">
          <span>Selected quantity</span>
          <strong>{{ selectedCount }}</strong>
        </div>

        <div class="cart-summary__row">
          <span>Total quantity</span>
          <strong>{{ cartStore.totalCount.value }}</strong>
        </div>

        <div class="cart-summary__row cart-summary__row--amount">
          <span>Selected amount</span>
          <strong>¥{{ formatCurrency(selectedAmount) }}</strong>
        </div>

        <el-button class="cart-summary__submit" type="warning" @click="handleCheckout">
          Checkout
        </el-button>
      </aside>
    </div>

    <section v-else class="cart-empty" v-loading="cartStore.state.loading">
      <el-empty description="Your cart is empty." />
      <el-button type="warning" @click="router.push('/user/home')">Go Shopping</el-button>
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
