<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRoute } from 'vue-router';

import {
  cancelOrder,
  completeOrder,
  confirmOrder,
  deliverOrder,
  getOrderDetail,
  getOrderPage,
  getOrderStatistics,
  rejectOrder
} from '@/api/order';

const route = useRoute();

const loading = ref(false);
const detailLoading = ref(false);
const actionLoading = ref(false);
const detailVisible = ref(false);

const page = ref(1);
const pageSize = ref(10);
const total = ref(0);
const tableData = ref([]);
const detailData = ref(null);
const dateRange = ref([]);

const filters = reactive({
  number: '',
  phone: '',
  status: ''
});

const statistics = reactive({
  toBeConfirmed: 0,
  confirmed: 0,
  deliveryInProgress: 0
});

const orderStatusMap = {
  1: { label: '待付款', type: 'warning' },
  2: { label: '待接单', type: 'danger' },
  3: { label: '待派送', type: 'primary' },
  4: { label: '派送中', type: 'success' },
  5: { label: '已完成', type: 'success' },
  6: { label: '已取消', type: 'info' }
};

const payStatusMap = {
  0: '未支付',
  1: '已支付',
  2: '已退款'
};

const statusTabs = computed(() => [
  { label: '全部订单', value: '', count: null },
  { label: '待接单', value: 2, count: statistics.toBeConfirmed },
  { label: '待派送', value: 3, count: statistics.confirmed },
  { label: '派送中', value: 4, count: statistics.deliveryInProgress },
  { label: '已完成', value: 5, count: null },
  { label: '已取消', value: 6, count: null }
]);

const formatDateTime = (value) => (value ? String(value).replace('T', ' ') : '--');
const formatAmount = (value) => Number(value || 0).toFixed(2);
const getStatusMeta = (status) => orderStatusMap[Number(status)] || { label: '未知', type: 'info' };

const buildQueryParams = () => ({
  page: page.value,
  pageSize: pageSize.value,
  number: filters.number || undefined,
  phone: filters.phone || undefined,
  status: filters.status === '' ? undefined : filters.status,
  beginTime: dateRange.value?.[0] || undefined,
  endTime: dateRange.value?.[1] || undefined
});

const fetchStatistics = async () => {
  const res = await getOrderStatistics();
  if (res.code === 1) {
    statistics.toBeConfirmed = res.data.toBeConfirmed || 0;
    statistics.confirmed = res.data.confirmed || 0;
    statistics.deliveryInProgress = res.data.deliveryInProgress || 0;
  }
};

const fetchOrders = async () => {
  loading.value = true;
  try {
    const res = await getOrderPage(buildQueryParams());
    if (res.code === 1) {
      tableData.value = res.data.records || [];
      total.value = res.data.total || 0;
    }
  } finally {
    loading.value = false;
  }
};

const fetchPageData = async () => {
  await Promise.all([fetchStatistics(), fetchOrders()]);
};

const handleSearch = async () => {
  page.value = 1;
  await fetchOrders();
};

const handleReset = async () => {
  filters.number = '';
  filters.phone = '';
  dateRange.value = [];
  page.value = 1;
  await fetchOrders();
};

const handleStatusTabChange = async (status) => {
  filters.status = status;
  page.value = 1;
  await fetchOrders();
};

const handlePageChange = async (value) => {
  page.value = value;
  await fetchOrders();
};

const handleSizeChange = async (value) => {
  pageSize.value = value;
  page.value = 1;
  await fetchOrders();
};

const openDetail = async (id) => {
  detailVisible.value = true;
  detailLoading.value = true;
  try {
    const res = await getOrderDetail(id);
    if (res.code === 1) {
      detailData.value = res.data || null;
    }
  } finally {
    detailLoading.value = false;
  }
};

const refreshAfterAction = async () => {
  await fetchPageData();
  if (detailVisible.value && detailData.value?.id) {
    await openDetail(detailData.value.id);
  }
};

const withAction = async (handler) => {
  actionLoading.value = true;
  try {
    await handler();
  } finally {
    actionLoading.value = false;
  }
};

const handleConfirm = async (row) => {
  await withAction(async () => {
    const res = await confirmOrder({ id: row.id, status: 3 });
    if (res.code === 1) {
      ElMessage.success('接单成功');
      await refreshAfterAction();
    }
  });
};

const handleReject = async (row) => {
  ElMessageBox.prompt('请输入拒单原因', '拒单', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '例如：库存不足 / 商家暂时无法接单'
  }).then(async ({ value }) => {
    await withAction(async () => {
      const res = await rejectOrder({
        id: row.id,
        rejectionReason: value
      });
      if (res.code === 1) {
        ElMessage.success('拒单成功');
        await refreshAfterAction();
      }
    });
  }).catch(() => {});
};

const handleCancel = async (row) => {
  ElMessageBox.prompt('请输入取消原因', '取消订单', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '例如：库存不足 / 配送范围调整'
  }).then(async ({ value }) => {
    await withAction(async () => {
      const res = await cancelOrder({
        id: row.id,
        cancelReason: value
      });
      if (res.code === 1) {
        ElMessage.success('取消成功');
        await refreshAfterAction();
      }
    });
  }).catch(() => {});
};

const handleDelivery = async (row) => {
  await withAction(async () => {
    const res = await deliverOrder(row.id);
    if (res.code === 1) {
      ElMessage.success('订单已进入派送中');
      await refreshAfterAction();
    }
  });
};

const handleComplete = async (row) => {
  await withAction(async () => {
    const res = await completeOrder(row.id);
    if (res.code === 1) {
      ElMessage.success('订单已完成');
      await refreshAfterAction();
    }
  });
};

const handleOrderNotification = async (event) => {
  const payload = event?.detail || {};
  if (![1, 2].includes(Number(payload.type))) {
    return;
  }

  await fetchPageData();
};

const openDetailByRoute = async () => {
  const orderId = Number(route.query.orderId || 0);
  if (!orderId) {
    return;
  }

  await openDetail(orderId);
};

const canConfirm = (row) => Number(row.status) === 2;
const canReject = (row) => Number(row.status) === 2;
const canCancel = (row) => [2, 3, 4].includes(Number(row.status));
const canDelivery = (row) => Number(row.status) === 3;
const canComplete = (row) => Number(row.status) === 4;

watch(
  () => [route.query.orderId, route.query.noticeAt],
  async ([orderId]) => {
    if (!orderId) {
      return;
    }

    await fetchPageData();
    await openDetail(Number(orderId));
  }
);

onMounted(async () => {
  window.addEventListener('admin-order-notification', handleOrderNotification);
  await fetchPageData();
  await openDetailByRoute();
});

onBeforeUnmount(() => {
  window.removeEventListener('admin-order-notification', handleOrderNotification);
});
</script>

<template>
  <div class="orders-page">
    <div class="orders-page__header">
      <div>
        <p class="orders-page__eyebrow">Order Center</p>
        <h2>订单管理</h2>
      </div>
      <el-button type="primary" @click="fetchPageData">刷新订单</el-button>
    </div>

    <div class="orders-page__tabs">
      <button
        v-for="item in statusTabs"
        :key="String(item.value)"
        type="button"
        class="orders-page__tab"
        :class="{ 'orders-page__tab--active': filters.status === item.value }"
        @click="handleStatusTabChange(item.value)"
      >
        {{ item.label }}<span v-if="item.count !== null">({{ item.count }})</span>
      </button>
    </div>

    <div class="orders-page__search">
      <div class="orders-page__search-row">
        <div class="orders-page__field">
          <label>订单号：</label>
          <el-input
            v-model="filters.number"
            placeholder="请输入订单号"
            clearable
            style="width: 180px"
            @keyup.enter="handleSearch"
          />
        </div>

        <div class="orders-page__field">
          <label>手机号：</label>
          <el-input
            v-model="filters.phone"
            placeholder="请输入手机号"
            clearable
            style="width: 180px"
            @keyup.enter="handleSearch"
          />
        </div>

        <div class="orders-page__field orders-page__field--time">
          <label>下单时间：</label>
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 360px"
          />
        </div>

        <div class="orders-page__actions">
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
      </div>
    </div>

    <div class="orders-page__table-wrap">
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        class="orders-page__table"
        :header-cell-style="{ background: '#f5f7fa', color: '#303133', fontWeight: 500 }"
      >
        <el-table-column prop="number" label="订单号" min-width="160" />
        <el-table-column label="订单状态" width="110">
          <template #default="{ row }">
            <span class="orders-page__status-text">{{ getStatusMeta(row.status).label }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="用户名" width="120">
          <template #default="{ row }">
            {{ row.userName || '--' }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="address" label="地址" min-width="320" show-overflow-tooltip />
        <el-table-column label="下单时间" min-width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.orderTime) }}
          </template>
        </el-table-column>
        <el-table-column label="实收金额" width="110">
          <template #default="{ row }">
            {{ formatAmount(row.amount) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="orders-page__operate">
              <el-button
                v-if="canConfirm(row)"
                link
                type="primary"
                :loading="actionLoading"
                @click="handleConfirm(row)"
              >
                接单
              </el-button>
              <el-button
                v-if="canReject(row)"
                link
                type="danger"
                :loading="actionLoading"
                @click="handleReject(row)"
              >
                拒单
              </el-button>
              <el-button
                v-if="canDelivery(row)"
                link
                type="primary"
                :loading="actionLoading"
                @click="handleDelivery(row)"
              >
                派送
              </el-button>
              <el-button
                v-if="canComplete(row)"
                link
                type="success"
                :loading="actionLoading"
                @click="handleComplete(row)"
              >
                完成
              </el-button>
              <el-button
                v-if="canCancel(row)"
                link
                type="warning"
                :loading="actionLoading"
                @click="handleCancel(row)"
              >
                取消
              </el-button>
              <el-button link type="primary" @click="openDetail(row.id)">查看</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="orders-page__pagination" v-if="total">
      <el-pagination
        :current-page="page"
        :page-size="pageSize"
        :page-sizes="[10, 20, 30]"
        :total="total"
        layout="prev, pager, next, sizes, jumper"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>

    <el-dialog
      v-model="detailVisible"
      title="订单详情"
      width="860px"
      destroy-on-close
    >
      <div v-if="detailData" v-loading="detailLoading" class="order-detail">
        <div class="order-detail__top">
          <div class="order-detail__card">
            <h3>基础信息</h3>
            <p>订单号：{{ detailData.number }}</p>
            <p>订单状态：{{ getStatusMeta(detailData.status).label }}</p>
            <p>支付状态：{{ payStatusMap[Number(detailData.payStatus)] || '未知' }}</p>
            <p>订单金额：￥{{ formatAmount(detailData.amount) }}</p>
          </div>

          <div class="order-detail__card">
            <h3>时间信息</h3>
            <p>下单时间：{{ formatDateTime(detailData.orderTime) }}</p>
            <p>支付时间：{{ formatDateTime(detailData.checkoutTime) }}</p>
            <p>预计送达：{{ formatDateTime(detailData.estimatedDeliveryTime) }}</p>
            <p>送达时间：{{ formatDateTime(detailData.deliveryTime) }}</p>
          </div>
        </div>

        <div class="order-detail__card">
          <h3>配送信息</h3>
          <p>用户：{{ detailData.userName || '--' }}</p>
          <p>收货人：{{ detailData.consignee || '--' }}</p>
          <p>手机号：{{ detailData.phone || '--' }}</p>
          <p>配送地址：{{ detailData.address || '--' }}</p>
          <p>备注：{{ detailData.remark || '无' }}</p>
        </div>

        <div class="order-detail__card">
          <h3>订单商品</h3>
          <div class="order-detail__items">
            <article
              v-for="item in detailData.orderDetailList || []"
              :key="item.id"
              class="order-detail__item"
            >
              <img :src="item.image" :alt="item.name" />
              <div>
                <strong>{{ item.name }}</strong>
                <span v-if="item.supplementDetail">{{ item.supplementDetail }}</span>
                <small>{{ item.number }} x ￥{{ formatAmount(item.amount) }}</small>
              </div>
            </article>
          </div>
        </div>

        <div
          v-if="detailData.cancelReason || detailData.rejectionReason"
          class="order-detail__card"
        >
          <h3>取消信息</h3>
          <p v-if="detailData.cancelReason">取消原因：{{ detailData.cancelReason }}</p>
          <p v-if="detailData.rejectionReason">拒单原因：{{ detailData.rejectionReason }}</p>
          <p>取消时间：{{ formatDateTime(detailData.cancelTime) }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.orders-page {
  padding: 30px;
  background: #f5f7fa;
}

.orders-page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.orders-page__eyebrow {
  margin: 0 0 8px;
  color: #909399;
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.orders-page__header h2 {
  margin: 0;
  font-size: 32px;
  color: #303133;
}

.orders-page__tabs,
.orders-page__search,
.orders-page__table-wrap {
  background: #ffffff;
  border-radius: 6px;
}

.orders-page__tabs {
  display: flex;
  flex-wrap: wrap;
  border: 1px solid #dcdfe6;
  overflow: hidden;
}

.orders-page__tab {
  min-width: 146px;
  height: 60px;
  padding: 0 18px;
  border: none;
  border-right: 1px solid #dcdfe6;
  background: #ffffff;
  color: #303133;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease;
}

.orders-page__tab:last-child {
  border-right: none;
}

.orders-page__tab--active {
  color: #409eff;
  background: #f5faff;
  box-shadow: inset 0 -2px 0 #409eff;
}

.orders-page__search {
  margin-top: 16px;
  padding: 20px 24px;
}

.orders-page__search-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 18px;
}

.orders-page__field {
  display: flex;
  align-items: center;
  gap: 12px;
}

.orders-page__field label {
  color: #303133;
  white-space: nowrap;
}

.orders-page__field--time {
  margin-left: 12px;
}

.orders-page__actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: auto;
}

.orders-page__table-wrap {
  margin-top: 16px;
  padding: 0 18px 20px;
}

.orders-page__table {
  width: 100%;
}

.orders-page__status-text {
  color: #303133;
}

.orders-page__operate {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
}

.orders-page__pagination {
  display: flex;
  justify-content: center;
  margin-top: 18px;
}

.orders-page :deep(.el-table td),
.orders-page :deep(.el-table th) {
  padding: 16px 0;
}

.order-detail {
  display: grid;
  gap: 18px;
}

.order-detail__top {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.order-detail__card {
  padding: 18px;
  background: #f5f7fa;
  border-radius: 8px;
}

.order-detail__card h3 {
  margin: 0 0 12px;
  color: #303133;
}

.order-detail__card p,
.order-detail__item span,
.order-detail__item small {
  margin: 0;
  color: #606266;
  line-height: 1.9;
}

.order-detail__items {
  display: grid;
  gap: 12px;
}

.order-detail__item {
  display: grid;
  grid-template-columns: 72px 1fr;
  gap: 12px;
  align-items: center;
  padding: 12px;
  background: #ffffff;
  border-radius: 8px;
}

.order-detail__item img {
  width: 72px;
  height: 72px;
  object-fit: cover;
  border-radius: 8px;
  background: #f2f3f5;
}

.order-detail__item strong,
.order-detail__item span,
.order-detail__item small {
  display: block;
}

@media (max-width: 1440px) {
  .orders-page__actions {
    margin-left: 0;
  }
}

@media (max-width: 1280px) {
  .orders-page__header,
  .orders-page__search-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .orders-page__field--time {
    margin-left: 0;
  }

  .order-detail__top {
    grid-template-columns: 1fr;
  }
}
</style>
