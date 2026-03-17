<script setup>
import { formatCurrency } from '@/utils/shop';

defineProps({
  totalAmount: {
    type: Number,
    default: 0
  },
  totalCount: {
    type: Number,
    default: 0
  }
});

defineEmits(['open-cart', 'checkout']);
</script>

<template>
  <div class="cart-dock">
    <button type="button" class="cart-dock__icon" @click="$emit('open-cart')">
      <el-icon><ShoppingCart /></el-icon>
    </button>

    <div class="cart-dock__summary">
      <p>已选 {{ totalCount }} 件商品</p>
      <strong>¥{{ formatCurrency(totalAmount) }}</strong>
    </div>

    <el-button type="warning" class="cart-dock__action" @click="$emit('checkout')">
      去结算
    </el-button>
  </div>
</template>

<style scoped>
.cart-dock {
  position: sticky;
  bottom: 24px;
  display: flex;
  align-items: center;
  gap: 18px;
  width: min(760px, calc(100% - 16px));
  margin: 26px auto 0;
  padding: 14px 18px;
  background: rgba(27, 20, 16, 0.92);
  border-radius: 999px;
  box-shadow: 0 18px 40px rgba(28, 16, 7, 0.24);
  backdrop-filter: blur(18px);
  z-index: 2;
}

.cart-dock__icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 50%;
  background: linear-gradient(135deg, #ffd25b, #ff9827);
  color: #3c2410;
  font-size: 24px;
  cursor: pointer;
}

.cart-dock__summary {
  flex: 1;
  color: #fff7ec;
}

.cart-dock__summary p {
  margin: 0 0 4px;
  color: rgba(255, 247, 236, 0.7);
  font-size: 13px;
}

.cart-dock__summary strong {
  font-size: 28px;
}

.cart-dock__action {
  min-width: 132px;
  height: 50px;
  border: none;
  border-radius: 999px;
  font-weight: 700;
}

@media (max-width: 768px) {
  .cart-dock {
    width: calc(100% - 12px);
    gap: 12px;
  }

  .cart-dock__summary strong {
    font-size: 22px;
  }
}
</style>

