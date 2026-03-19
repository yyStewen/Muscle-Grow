<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';

import { cancelOrder, getOrderDetail, payOrder, remindOrder, repeatOrder } from '@/api/order';
import { useCartStore } from '@/stores/cart';
import {
  buildOrderSummary,
  formatOrderDateTime,
  hasRepeatableOrderItems,
  getOrderStatusMeta,
  getPaymentStatusMeta
} from '@/utils/order';
import { formatCurrency } from '@/utils/shop';

const route = useRoute();
const router = useRouter();
const cartStore = useCartStore();

const loading = ref(false);
const actionLoading = ref(false);
const order = ref(null);

const orderId = computed(() => Number(route.params.id || 0));
const statusMeta = computed(() => getOrderStatusMeta(order.value?.status));
const paymentMeta = computed(() => getPaymentStatusMeta(order.value?.payStatus));

const fetchDetail = async () => {
  if (!orderId.value) return;

  loading.value = true;

  try {
    const res = await getOrderDetail(orderId.value);
    if (res.code === 1) {
      order.value = res.data || null;
    }
  } finally {
    loading.value = false;
  }
};

watch(orderId, fetchDetail);

const withAction = async (handler) => {
  actionLoading.value = true;
  try {
    await handler();
  } finally {
    actionLoading.value = false;
  }
};

const handlePay = async () => {
  if (!order.value) return;

  await withAction(async () => {
    const res = await payOrder({
      orderNumber: order.value.number,
      payMethod: 1
    });

    if (res.code === 1) {
      ElMessage.success('Payment completed.');
      await fetchDetail();
    }
  });
};

const handleCancel = async () => {
  if (!order.value) return;

  ElMessageBox.confirm('Cancel this order?', 'Confirm', {
    confirmButtonText: 'Cancel Order',
    cancelButtonText: 'Back',
    type: 'warning'
  }).then(async () => {
    await withAction(async () => {
      const res = await cancelOrder(order.value.id);
      if (res.code === 1) {
        ElMessage.success('Order cancelled.');
        await fetchDetail();
      }
    });
  }).catch(() => {});
};

const handleRepeat = async () => {
  if (!order.value) return;

  await withAction(async () => {
    const res = await repeatOrder(order.value.id);
    if (res.code === 1) {
      await cartStore.fetchCart();
      ElMessage.success('Items were added back to your cart.');
      router.push('/user/cart');
    }
  });
};

const handleRemind = async () => {
  if (!order.value) return;

  await withAction(async () => {
    const res = await remindOrder(order.value.id);
    if (res.code === 1) {
      ElMessage.success('Merchant reminder sent.');
    }
  });
};

const canPay = computed(() => Number(order.value?.status) === 1);
const canCancel = computed(() => [1, 2].includes(Number(order.value?.status)));
const canRemind = computed(() => [2, 3, 4].includes(Number(order.value?.status)));
const canRepeat = computed(() =>
  [5, 6].includes(Number(order.value?.status)) && hasRepeatableOrderItems(order.value)
);

onMounted(async () => {
  await fetchDetail();
});
</script>

<template>
  <div class="order-detail" v-loading="loading">
    <section class="order-detail__hero">
      <div>
        <p class="order-detail__eyebrow">Order Detail</p>
        <h2>{{ order ? `Order ${order.number}` : 'Order Detail' }}</h2>
        <span>
          Inspect merchant progress, payment status, address information, and every item in this order.
        </span>
      </div>

      <div class="order-detail__hero-actions">
        <el-button @click="router.push('/user/orders')">Back To Orders</el-button>
        <el-button v-if="canPay" type="warning" :loading="actionLoading" @click="handlePay">Pay</el-button>
        <el-button v-if="canCancel" plain type="danger" :loading="actionLoading" @click="handleCancel">
          Cancel
        </el-button>
        <el-button v-if="canRemind" plain type="warning" :loading="actionLoading" @click="handleRemind">
          Remind
        </el-button>
        <el-button v-if="canRepeat" plain type="success" :loading="actionLoading" @click="handleRepeat">
          Repeat
        </el-button>
      </div>
    </section>

    <section v-if="order" class="order-detail__grid">
      <section class="order-detail__main">
        <div class="order-detail__panel">
          <div class="order-detail__panel-head">
            <h3>Overview</h3>
            <div class="order-detail__tags">
              <el-tag :type="statusMeta.type" effect="dark">{{ statusMeta.label }}</el-tag>
              <el-tag :type="paymentMeta.type" plain>{{ paymentMeta.label }}</el-tag>
            </div>
          </div>

          <div class="order-detail__info-grid">
            <div>
              <label>Order Number</label>
              <strong>{{ order.number }}</strong>
            </div>
            <div>
              <label>Order Amount</label>
              <strong>¥{{ formatCurrency(order.amount) }}</strong>
            </div>
            <div>
              <label>Placed Time</label>
              <strong>{{ formatOrderDateTime(order.orderTime) }}</strong>
            </div>
            <div>
              <label>Paid Time</label>
              <strong>{{ formatOrderDateTime(order.checkoutTime) }}</strong>
            </div>
            <div>
              <label>Estimated Delivery</label>
              <strong>{{ formatOrderDateTime(order.estimatedDeliveryTime) }}</strong>
            </div>
            <div>
              <label>Delivery Time</label>
              <strong>{{ formatOrderDateTime(order.deliveryTime) }}</strong>
            </div>
          </div>
        </div>

        <div class="order-detail__panel">
          <div class="order-detail__panel-head">
            <h3>Items</h3>
            <span>{{ buildOrderSummary(order) }}</span>
          </div>

          <div class="order-detail__items">
            <article
              v-for="item in order.orderDetailList || []"
              :key="item.id"
              class="order-item"
            >
              <img v-if="item.image" :src="item.image" :alt="item.name" />
              <div v-else class="order-item__placeholder">Voucher</div>
              <div class="order-item__body">
                <strong>{{ item.name }}</strong>
                <span v-if="item.supplementDetail">{{ item.supplementDetail }}</span>
                <small>{{ item.number }} x ¥{{ formatCurrency(item.amount) }}</small>
              </div>
            </article>
          </div>
        </div>
      </section>

      <aside class="order-detail__side">
        <div class="order-detail__panel">
          <div class="order-detail__panel-head">
            <h3>Shipping</h3>
          </div>

          <div class="order-detail__shipping">
            <strong>{{ order.consignee || '--' }} / {{ order.phone || '--' }}</strong>
            <span>{{ order.address || '--' }}</span>
          </div>
        </div>

        <div class="order-detail__panel">
          <div class="order-detail__panel-head">
            <h3>Note</h3>
          </div>
          <p class="order-detail__text">{{ order.remark || 'No merchant note.' }}</p>
        </div>

        <div v-if="order.cancelReason || order.rejectionReason" class="order-detail__panel">
          <div class="order-detail__panel-head">
            <h3>Cancellation Info</h3>
          </div>
          <p v-if="order.cancelReason" class="order-detail__text">Cancel reason: {{ order.cancelReason }}</p>
          <p v-if="order.rejectionReason" class="order-detail__text">Rejection reason: {{ order.rejectionReason }}</p>
          <p class="order-detail__text">Cancel time: {{ formatOrderDateTime(order.cancelTime) }}</p>
        </div>
      </aside>
    </section>

    <section v-else class="order-detail__empty">
      <el-empty description="Order not found." />
      <el-button type="warning" @click="router.push('/user/orders')">Back To Orders</el-button>
    </section>
  </div>
</template>

<style scoped>
.order-detail {
  animation: fadeUp 0.45s ease;
}

.order-detail__hero,
.order-detail__panel,
.order-detail__empty {
  background: rgba(255, 251, 244, 0.94);
  border: 1px solid rgba(186, 145, 83, 0.14);
  border-radius: 28px;
}

.order-detail__hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 28px;
}

.order-detail__eyebrow {
  margin: 0 0 8px;
  color: #b17b3f;
  font-size: 12px;
  letter-spacing: 0.22em;
  text-transform: uppercase;
}

.order-detail__hero h2 {
  margin: 0 0 10px;
  font-size: 34px;
  color: #321f12;
}

.order-detail__hero span {
  color: #7a5b3e;
  line-height: 1.7;
}

.order-detail__hero-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.order-detail__grid {
  display: grid;
  grid-template-columns: 1.28fr 0.82fr;
  gap: 20px;
  margin-top: 20px;
}

.order-detail__main,
.order-detail__side {
  display: grid;
  gap: 20px;
}

.order-detail__panel {
  padding: 24px;
}

.order-detail__panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.order-detail__panel-head h3 {
  margin: 0;
  color: #311f12;
  font-size: 26px;
}

.order-detail__panel-head span {
  color: #8d6b47;
  line-height: 1.7;
}

.order-detail__tags {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.order-detail__info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
  margin-top: 20px;
}

.order-detail__info-grid label,
.order-detail__shipping span,
.order-detail__text {
  color: #866447;
}

.order-detail__info-grid label {
  display: block;
  margin-bottom: 8px;
  font-size: 13px;
}

.order-detail__info-grid strong {
  color: #2f1d10;
}

.order-detail__items {
  display: grid;
  gap: 14px;
  margin-top: 20px;
}

.order-item {
  display: grid;
  grid-template-columns: 84px 1fr;
  gap: 16px;
  align-items: center;
  padding: 14px;
  background: #ffffff;
  border-radius: 20px;
}

.order-item img {
  width: 84px;
  height: 84px;
  object-fit: cover;
  border-radius: 18px;
  background: #fff0dc;
}

.order-item__placeholder {
  display: grid;
  place-items: center;
  width: 84px;
  height: 84px;
  border-radius: 18px;
  background: linear-gradient(135deg, #fff0dc, #ffe2bf);
  color: #b16b22;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.order-item__body strong,
.order-item__body span,
.order-item__body small,
.order-detail__shipping strong,
.order-detail__shipping span {
  display: block;
}

.order-item__body span,
.order-item__body small,
.order-detail__shipping span,
.order-detail__text {
  margin-top: 8px;
  line-height: 1.7;
}

.order-detail__empty {
  margin-top: 20px;
  padding: 30px;
  display: grid;
  place-items: center;
  gap: 12px;
}

@media (max-width: 1300px) {
  .order-detail__hero,
  .order-detail__grid,
  .order-detail__panel-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .order-detail__grid,
  .order-detail__main,
  .order-detail__side {
    grid-template-columns: 1fr;
  }

  .order-detail__info-grid {
    grid-template-columns: 1fr;
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
