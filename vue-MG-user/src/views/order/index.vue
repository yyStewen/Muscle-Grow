<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';

import { cancelOrder, getOrderHistory, payOrder, remindOrder, repeatOrder } from '@/api/order';
import { useCartStore } from '@/stores/cart';
import {
  buildOrderSummary,
  formatOrderDateTime,
  getOrderStatusMeta,
  getPaymentStatusMeta
} from '@/utils/order';
import { formatCurrency } from '@/utils/shop';

const router = useRouter();
const cartStore = useCartStore();

const loading = ref(false);
const actionLoading = ref(false);
const page = ref(1);
const pageSize = ref(8);
const total = ref(0);
const orders = ref([]);
const activeStatus = ref('');

const statusTabs = [
  { label: 'All Orders', value: '' },
  { label: 'Pending Payment', value: 1 },
  { label: 'Pending Merchant', value: 2 },
  { label: 'Accepted', value: 3 },
  { label: 'In Delivery', value: 4 },
  { label: 'Completed', value: 5 },
  { label: 'Cancelled', value: 6 }
];

const emptyText = computed(() =>
  activeStatus.value === '' ? 'No orders yet.' : 'No orders found for the current status.'
);

const fetchOrders = async () => {
  loading.value = true;

  try {
    const res = await getOrderHistory({
      page: page.value,
      pageSize: pageSize.value,
      status: activeStatus.value === '' ? undefined : activeStatus.value
    });

    if (res.code === 1) {
      orders.value = res.data.records || [];
      total.value = res.data.total || 0;
    }
  } finally {
    loading.value = false;
  }
};

watch(activeStatus, () => {
  page.value = 1;
  fetchOrders();
});

const handlePageChange = (value) => {
  page.value = value;
  fetchOrders();
};

const withAction = async (handler) => {
  actionLoading.value = true;
  try {
    await handler();
  } finally {
    actionLoading.value = false;
  }
};

const handlePay = async (order) => {
  await withAction(async () => {
    const res = await payOrder({
      orderNumber: order.number,
      payMethod: 1
    });

    if (res.code === 1) {
      ElMessage.success('Payment completed.');
      await fetchOrders();
    }
  });
};

const handleCancel = async (order) => {
  ElMessageBox.confirm('Cancel this order?', 'Confirm', {
    confirmButtonText: 'Cancel Order',
    cancelButtonText: 'Back',
    type: 'warning'
  }).then(async () => {
    await withAction(async () => {
      const res = await cancelOrder(order.id);
      if (res.code === 1) {
        ElMessage.success('Order cancelled.');
        await fetchOrders();
      }
    });
  }).catch(() => {});
};

const handleRepeat = async (order) => {
  await withAction(async () => {
    const res = await repeatOrder(order.id);
    if (res.code === 1) {
      await cartStore.fetchCart();
      ElMessage.success('Items were added back to your cart.');
      router.push('/user/cart');
    }
  });
};

const handleRemind = async (order) => {
  await withAction(async () => {
    const res = await remindOrder(order.id);
    if (res.code === 1) {
      ElMessage.success('Merchant reminder sent.');
    }
  });
};

const goDetail = (id) => {
  router.push(`/user/orders/${id}`);
};

const canPay = (order) => Number(order.status) === 1;
const canCancel = (order) => [1, 2].includes(Number(order.status));
const canRemind = (order) => [2, 3, 4].includes(Number(order.status));
const canRepeat = (order) => [5, 6].includes(Number(order.status));

onMounted(async () => {
  await fetchOrders();
});
</script>

<template>
  <div class="order-page">
    <section class="order-page__hero">
      <div>
        <p class="order-page__eyebrow">My Orders</p>
        <h2>Track Every Order Step</h2>
        <span>
          Review payment status, follow merchant handling progress, and reorder completed plans.
        </span>
      </div>

      <div class="order-page__hero-actions">
        <el-button @click="router.push('/user/home')">Continue Shopping</el-button>
        <el-button type="warning" @click="fetchOrders">Refresh</el-button>
      </div>
    </section>

    <section class="order-filter">
      <el-radio-group v-model="activeStatus" class="order-filter__tabs">
        <el-radio-button
          v-for="item in statusTabs"
          :key="String(item.value)"
          :label="item.value"
        >
          {{ item.label }}
        </el-radio-button>
      </el-radio-group>
    </section>

    <section class="order-list" v-loading="loading">
      <article v-for="order in orders" :key="order.id" class="order-card">
        <div class="order-card__top">
          <div>
            <div class="order-card__headline">
              <h3>Order {{ order.number }}</h3>
              <el-tag :type="getOrderStatusMeta(order.status).type" effect="dark">
                {{ getOrderStatusMeta(order.status).label }}
              </el-tag>
              <el-tag :type="getPaymentStatusMeta(order.payStatus).type" plain>
                {{ getPaymentStatusMeta(order.payStatus).label }}
              </el-tag>
            </div>

            <p class="order-card__meta">
              <span>Placed {{ formatOrderDateTime(order.orderTime) }}</span>
              <span v-if="order.checkoutTime">Paid {{ formatOrderDateTime(order.checkoutTime) }}</span>
              <span>Amount ¥{{ formatCurrency(order.amount) }}</span>
            </p>
          </div>

          <div class="order-card__actions">
            <el-button plain @click="goDetail(order.id)">Details</el-button>
            <el-button v-if="canPay(order)" type="warning" :loading="actionLoading" @click="handlePay(order)">
              Pay
            </el-button>
            <el-button
              v-if="canCancel(order)"
              plain
              type="danger"
              :loading="actionLoading"
              @click="handleCancel(order)"
            >
              Cancel
            </el-button>
            <el-button
              v-if="canRemind(order)"
              plain
              type="warning"
              :loading="actionLoading"
              @click="handleRemind(order)"
            >
              Remind
            </el-button>
            <el-button
              v-if="canRepeat(order)"
              plain
              type="success"
              :loading="actionLoading"
              @click="handleRepeat(order)"
            >
              Repeat
            </el-button>
          </div>
        </div>

        <p class="order-card__summary">{{ buildOrderSummary(order) || 'No order item summary.' }}</p>
        <p class="order-card__address">{{ order.address || 'No shipping address recorded.' }}</p>

        <div v-if="order.cancelReason || order.rejectionReason" class="order-card__notice">
          <span v-if="order.cancelReason">Cancel reason: {{ order.cancelReason }}</span>
          <span v-if="order.rejectionReason">Rejection reason: {{ order.rejectionReason }}</span>
        </div>
      </article>

      <section v-if="!orders.length" class="order-page__empty">
        <el-empty :description="emptyText" />
        <el-button type="warning" @click="router.push('/user/home')">Browse Supplements</el-button>
      </section>
    </section>

    <div v-if="total" class="order-page__pagination">
      <span>Total {{ total }} items</span>
      <el-pagination
        layout="prev, pager, next"
        :current-page="page"
        :page-size="pageSize"
        :total="total"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<style scoped>
.order-page {
  animation: fadeUp 0.45s ease;
}

.order-page__hero,
.order-filter,
.order-list,
.order-page__empty {
  background: rgba(255, 251, 244, 0.94);
  border: 1px solid rgba(186, 145, 83, 0.14);
  border-radius: 28px;
}

.order-page__hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 28px;
}

.order-page__eyebrow {
  margin: 0 0 8px;
  color: #b17b3f;
  font-size: 12px;
  letter-spacing: 0.22em;
  text-transform: uppercase;
}

.order-page__hero h2 {
  margin: 0 0 10px;
  font-size: 34px;
  color: #321f12;
}

.order-page__hero span {
  color: #7a5b3e;
  line-height: 1.7;
}

.order-page__hero-actions {
  display: flex;
  gap: 12px;
}

.order-filter {
  margin-top: 20px;
  padding: 18px 22px;
}

.order-filter__tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.order-list {
  margin-top: 20px;
  padding: 24px;
  display: grid;
  gap: 16px;
}

.order-card {
  padding: 22px;
  background: #ffffff;
  border-radius: 24px;
  box-shadow: 0 10px 30px rgba(113, 76, 31, 0.08);
}

.order-card__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
}

.order-card__headline {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.order-card__headline h3 {
  margin: 0;
  color: #311f12;
  font-size: 24px;
}

.order-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin: 12px 0 0;
  color: #8b6946;
}

.order-card__actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.order-card__summary,
.order-card__address {
  margin: 16px 0 0;
  color: #6f5237;
  line-height: 1.8;
}

.order-card__notice {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 16px;
  color: #b1602b;
}

.order-page__empty {
  padding: 30px;
  display: grid;
  place-items: center;
  gap: 12px;
}

.order-page__pagination {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 20px;
}

@media (max-width: 1240px) {
  .order-page__hero,
  .order-card__top {
    flex-direction: column;
    align-items: flex-start;
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
