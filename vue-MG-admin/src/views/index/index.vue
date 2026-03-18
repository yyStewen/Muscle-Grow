<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';

import ChartPanel from '@/components/report/ChartPanel.vue';
import {
  getBusinessData,
  getOrderOverview,
  getSetmealOverview,
  getSupplementOverview
} from '@/api/workspace';

const loading = ref(false);
const currentTime = ref('');
const lastUpdatedAt = ref('--');

const businessData = reactive({
  turnover: 0,
  validOrderCount: 0,
  orderCompletionRate: 0,
  unitPrice: 0,
  newUsers: 0
});

const orderOverview = reactive({
  waitingOrders: 0,
  deliveredOrders: 0,
  completedOrders: 0,
  cancelledOrders: 0,
  allOrders: 0
});

const supplementOverview = reactive({
  sold: 0,
  discontinued: 0
});

const setmealOverview = reactive({
  sold: 0,
  discontinued: 0
});

let clockTimer = null;

const numberValue = (value) => {
  const nextValue = Number(value);
  return Number.isFinite(nextValue) ? nextValue : 0;
};

const formatCurrency = (value) => `\u00A5${numberValue(value).toFixed(2)}`;

const formatPercent = (value) => `${(numberValue(value) * 100).toFixed(1)}%`;

const formatDateTime = (date) => {
  const current = date instanceof Date ? date : new Date(date);
  const year = current.getFullYear();
  const month = `${current.getMonth() + 1}`.padStart(2, '0');
  const day = `${current.getDate()}`.padStart(2, '0');
  const hour = `${current.getHours()}`.padStart(2, '0');
  const minute = `${current.getMinutes()}`.padStart(2, '0');
  const second = `${current.getSeconds()}`.padStart(2, '0');

  return `${year}-${month}-${day} ${hour}:${minute}:${second}`;
};

const syncClock = () => {
  currentTime.value = formatDateTime(new Date());
};

const updateLastUpdatedAt = () => {
  lastUpdatedAt.value = formatDateTime(new Date());
};

const summaryCards = computed(() => [
  {
    title: '今日营业额',
    value: formatCurrency(businessData.turnover),
    desc: '今日已完成订单的实收金额',
    accent: '#f97316',
    tint: 'rgba(249, 115, 22, 0.12)'
  },
  {
    title: '今日有效订单',
    value: `${numberValue(businessData.validOrderCount)}`,
    desc: '状态为已完成的订单数',
    accent: '#2563eb',
    tint: 'rgba(37, 99, 235, 0.12)'
  },
  {
    title: '订单完成率',
    value: formatPercent(businessData.orderCompletionRate),
    desc: '有效订单数 / 今日订单总数',
    accent: '#0f766e',
    tint: 'rgba(15, 118, 110, 0.12)'
  },
  {
    title: '平均客单价',
    value: formatCurrency(businessData.unitPrice),
    desc: '今日营业额 / 有效订单数',
    accent: '#7c3aed',
    tint: 'rgba(124, 58, 237, 0.12)'
  },
  {
    title: '今日新增用户',
    value: `${numberValue(businessData.newUsers)}`,
    desc: '今天注册并进入系统的用户数',
    accent: '#059669',
    tint: 'rgba(5, 150, 105, 0.12)'
  }
]);

const orderLegend = computed(() => [
  { name: '待接单', value: numberValue(orderOverview.waitingOrders), color: '#f59e0b' },
  { name: '待派送', value: numberValue(orderOverview.deliveredOrders), color: '#3b82f6' },
  { name: '已完成', value: numberValue(orderOverview.completedOrders), color: '#10b981' },
  { name: '已取消', value: numberValue(orderOverview.cancelledOrders), color: '#94a3b8' }
]);

const supplementLegend = computed(() => [
  { name: '起售中补剂', value: numberValue(supplementOverview.sold), color: '#22c55e' },
  { name: '已停售补剂', value: numberValue(supplementOverview.discontinued), color: '#f97316' }
]);

const setmealLegend = computed(() => [
  { name: '起售中套餐', value: numberValue(setmealOverview.sold), color: '#14b8a6' },
  { name: '已停售套餐', value: numberValue(setmealOverview.discontinued), color: '#64748b' }
]);

const buildPieOption = ({ total, subtitle, items }) => ({
  tooltip: {
    trigger: 'item',
    formatter: '{b}: {c}'
  },
  series: [
    {
      type: 'pie',
      radius: ['58%', '76%'],
      center: ['50%', '44%'],
      silent: true,
      label: {
        show: false
      },
      labelLine: {
        show: false
      },
      itemStyle: {
        borderColor: '#ffffff',
        borderWidth: 4
      },
      emphasis: {
        scale: false
      },
      showEmptyCircle: true,
      emptyCircleStyle: {
        color: '#e5e7eb'
      },
      data: items.map((item) => ({
        name: item.name,
        value: item.value,
        itemStyle: {
          color: item.color
        }
      }))
    }
  ],
  graphic: [
    {
      type: 'text',
      left: 'center',
      top: '36%',
      style: {
        text: `${numberValue(total)}`,
        fill: '#0f172a',
        fontSize: 28,
        fontWeight: 700,
        textAlign: 'center'
      }
    },
    {
      type: 'text',
      left: 'center',
      top: '49%',
      style: {
        text: subtitle,
        fill: '#64748b',
        fontSize: 13,
        textAlign: 'center'
      }
    }
  ]
});

const orderChartOption = computed(() =>
  buildPieOption({
    total: orderOverview.allOrders,
    subtitle: '今日订单',
    items: orderLegend.value
  })
);

const supplementChartOption = computed(() =>
  buildPieOption({
    total: numberValue(supplementOverview.sold) + numberValue(supplementOverview.discontinued),
    subtitle: '补剂总数',
    items: supplementLegend.value
  })
);

const setmealChartOption = computed(() =>
  buildPieOption({
    total: numberValue(setmealOverview.sold) + numberValue(setmealOverview.discontinued),
    subtitle: '套餐总数',
    items: setmealLegend.value
  })
);

const applyResponseData = (target, payload) => {
  Object.keys(target).forEach((key) => {
    target[key] = payload?.[key] ?? 0;
  });
};

const fetchWorkspace = async () => {
  loading.value = true;

  try {
    const [businessRes, orderRes, supplementRes, setmealRes] = await Promise.all([
      getBusinessData(),
      getOrderOverview(),
      getSupplementOverview(),
      getSetmealOverview()
    ]);

    const responses = [businessRes, orderRes, supplementRes, setmealRes];
    const hasError = responses.some((item) => item?.code !== 1);

    if (hasError) {
      ElMessage.warning('工作台部分数据加载失败，请稍后重试');
    }

    if (businessRes?.code === 1) {
      applyResponseData(businessData, businessRes.data);
    }

    if (orderRes?.code === 1) {
      applyResponseData(orderOverview, orderRes.data);
    }

    if (supplementRes?.code === 1) {
      applyResponseData(supplementOverview, supplementRes.data);
    }

    if (setmealRes?.code === 1) {
      applyResponseData(setmealOverview, setmealRes.data);
    }

    updateLastUpdatedAt();
  } finally {
    loading.value = false;
  }
};

onMounted(async () => {
  syncClock();
  clockTimer = window.setInterval(syncClock, 1000);
  await fetchWorkspace();
});

onBeforeUnmount(() => {
  if (clockTimer) {
    window.clearInterval(clockTimer);
    clockTimer = null;
  }
});
</script>

<template>
  <div class="workspace-page">
    <section class="workspace-page__hero">
      <div class="workspace-page__hero-main">
        <p class="workspace-page__eyebrow">Workspace Overview</p>
        <h2>工作台信息总览</h2>
        <span>
          汇总今日经营数据、订单状态、补剂状态与套餐状态，帮助你快速判断门店当前运行情况。
        </span>
      </div>

      <div class="workspace-page__hero-side">
        <div class="workspace-page__clock">
          <strong>{{ currentTime }}</strong>
          <span>最近刷新：{{ lastUpdatedAt }}</span>
        </div>
        <el-button type="primary" :loading="loading" @click="fetchWorkspace">
          刷新数据
        </el-button>
      </div>
    </section>

    <section class="workspace-page__stats">
      <article
        v-for="item in summaryCards"
        :key="item.title"
        class="summary-card"
        :style="{ '--accent': item.accent, '--tint': item.tint }"
      >
        <span>{{ item.title }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.desc }}</small>
      </article>
    </section>

    <section class="workspace-page__boards">
      <article class="overview-card">
        <div class="overview-card__head">
          <div>
            <p>Order Overview</p>
            <h3>今日订单总览</h3>
          </div>
          <span>统计口径：今日 00:00 至当前</span>
        </div>
        <ChartPanel :option="orderChartOption" :loading="loading" height="300px" />
        <div class="overview-card__list">
          <div v-for="item in orderLegend" :key="item.name" class="overview-card__item">
            <i :style="{ background: item.color }"></i>
            <span>{{ item.name }}</span>
            <strong>{{ item.value }}</strong>
          </div>
        </div>
      </article>

      <article class="overview-card">
        <div class="overview-card__head">
          <div>
            <p>Supplement Overview</p>
            <h3>补剂总览</h3>
          </div>
          <span>按起售状态统计全部补剂</span>
        </div>
        <ChartPanel :option="supplementChartOption" :loading="loading" height="300px" />
        <div class="overview-card__list overview-card__list--compact">
          <div v-for="item in supplementLegend" :key="item.name" class="overview-card__item">
            <i :style="{ background: item.color }"></i>
            <span>{{ item.name }}</span>
            <strong>{{ item.value }}</strong>
          </div>
        </div>
      </article>

      <article class="overview-card">
        <div class="overview-card__head">
          <div>
            <p>Setmeal Overview</p>
            <h3>套餐总览</h3>
          </div>
          <span>按起售状态统计全部套餐</span>
        </div>
        <ChartPanel :option="setmealChartOption" :loading="loading" height="300px" />
        <div class="overview-card__list overview-card__list--compact">
          <div v-for="item in setmealLegend" :key="item.name" class="overview-card__item">
            <i :style="{ background: item.color }"></i>
            <span>{{ item.name }}</span>
            <strong>{{ item.value }}</strong>
          </div>
        </div>
      </article>
    </section>
  </div>
</template>

<style scoped>
.workspace-page {
  display: grid;
  gap: 20px;
  padding: 28px;
  background: #f5f7fa;
}

.workspace-page__hero,
.summary-card,
.overview-card {
  border-radius: 18px;
  box-shadow: 0 14px 36px rgba(15, 23, 42, 0.08);
}

.workspace-page__hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 28px 32px;
  background: linear-gradient(135deg, #0f172a 0%, #155e75 55%, #0891b2 100%);
  color: #ffffff;
}

.workspace-page__hero-main {
  max-width: 760px;
}

.workspace-page__eyebrow {
  margin: 0 0 10px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 12px;
  letter-spacing: 0.22em;
  text-transform: uppercase;
}

.workspace-page__hero h2 {
  margin: 0 0 12px;
  font-size: 34px;
}

.workspace-page__hero span {
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.78);
}

.workspace-page__hero-side {
  display: grid;
  gap: 16px;
  min-width: 280px;
  justify-items: end;
}

.workspace-page__clock {
  display: grid;
  gap: 6px;
  padding: 18px 20px;
  width: 100%;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(8px);
}

.workspace-page__clock strong {
  font-size: 24px;
  line-height: 1.2;
}

.workspace-page__clock span {
  color: rgba(255, 255, 255, 0.74);
}

.workspace-page__stats {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 16px;
}

.summary-card {
  position: relative;
  overflow: hidden;
  padding: 24px;
  background: #ffffff;
}

.summary-card::before {
  content: '';
  position: absolute;
  inset: 0 auto 0 0;
  width: 6px;
  background: var(--accent);
}

.summary-card::after {
  content: '';
  position: absolute;
  width: 120px;
  height: 120px;
  right: -24px;
  top: -40px;
  border-radius: 50%;
  background: var(--tint);
}

.summary-card span,
.summary-card small {
  position: relative;
  z-index: 1;
}

.summary-card span {
  display: block;
  margin-bottom: 18px;
  color: #64748b;
}

.summary-card strong {
  position: relative;
  z-index: 1;
  display: block;
  margin-bottom: 12px;
  color: #0f172a;
  font-size: 32px;
  line-height: 1.2;
}

.summary-card small {
  display: block;
  color: #94a3b8;
  line-height: 1.6;
}

.workspace-page__boards {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
}

.overview-card {
  padding: 22px;
  background: #ffffff;
}

.overview-card__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 8px;
}

.overview-card__head p {
  margin: 0 0 8px;
  color: #f97316;
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.overview-card__head h3 {
  margin: 0;
  color: #0f172a;
  font-size: 24px;
}

.overview-card__head span {
  color: #94a3b8;
  line-height: 1.6;
  text-align: right;
}

.overview-card__list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 10px;
}

.overview-card__list--compact {
  grid-template-columns: 1fr;
}

.overview-card__item {
  display: grid;
  grid-template-columns: 12px 1fr auto;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  border-radius: 12px;
  background: #f8fafc;
}

.overview-card__item i {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.overview-card__item span {
  color: #475569;
}

.overview-card__item strong {
  color: #0f172a;
  font-size: 18px;
}

@media (max-width: 1480px) {
  .workspace-page__stats {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .workspace-page__boards {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 1200px) {
  .workspace-page__hero,
  .overview-card__head {
    flex-direction: column;
    align-items: flex-start;
  }

  .workspace-page__hero-side {
    width: 100%;
    justify-items: stretch;
  }

  .overview-card__head span {
    text-align: left;
  }

  .workspace-page__stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .workspace-page {
    padding: 18px;
  }

  .workspace-page__stats,
  .overview-card__list {
    grid-template-columns: 1fr;
  }

  .workspace-page__hero {
    padding: 22px;
  }

  .workspace-page__hero h2 {
    font-size: 28px;
  }
}
</style>
