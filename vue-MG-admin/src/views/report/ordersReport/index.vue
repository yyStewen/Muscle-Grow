<script setup>
import { computed, onMounted, ref } from 'vue';

import ChartPanel from '@/components/report/ChartPanel.vue';
import { getOrdersStatistics } from '@/api/report';
import { buildDateShortcuts, getRecentDaysRange, parseCsvList, toNumber } from '@/utils/report';

const loading = ref(false);
const dateRange = ref(getRecentDaysRange(7));
const shortcuts = buildDateShortcuts();
const dates = ref([]);
const orderCountList = ref([]);
const validOrderCountList = ref([]);
const totalOrderCount = ref(0);
const validOrderCount = ref(0);
const orderCompletionRate = ref(0);

const invalidOrderCount = computed(() =>
  Math.max(totalOrderCount.value - validOrderCount.value, 0)
);

const chartOption = computed(() => ({
  color: ['#64748b', '#22c55e'],
  tooltip: {
    trigger: 'axis'
  },
  legend: {
    top: 0
  },
  grid: {
    left: 56,
    right: 32,
    top: 56,
    bottom: 48
  },
  xAxis: {
    type: 'category',
    data: dates.value
  },
  yAxis: {
    type: 'value',
    name: '订单量',
    splitLine: {
      lineStyle: {
        color: '#e5e7eb'
      }
    }
  },
  series: [
    {
      name: '订单总数',
      type: 'bar',
      barGap: '30%',
      data: orderCountList.value,
      itemStyle: {
        borderRadius: [8, 8, 0, 0]
      }
    },
    {
      name: '有效订单',
      type: 'bar',
      data: validOrderCountList.value,
      itemStyle: {
        borderRadius: [8, 8, 0, 0]
      }
    }
  ]
}));

const fetchReport = async () => {
  if (!dateRange.value?.length) {
    return;
  }

  loading.value = true;
  try {
    const res = await getOrdersStatistics({
      begin: dateRange.value[0],
      end: dateRange.value[1]
    });

    if (res.code === 1) {
      dates.value = parseCsvList(res.data?.dateList);
      orderCountList.value = parseCsvList(res.data?.orderCountList, toNumber);
      validOrderCountList.value = parseCsvList(res.data?.validOrderCountList, toNumber);
      totalOrderCount.value = toNumber(res.data?.totalOrderCount);
      validOrderCount.value = toNumber(res.data?.validOrderCount);
      orderCompletionRate.value = Number(res.data?.orderCompletionRate || 0);
    }
  } finally {
    loading.value = false;
  }
};

onMounted(fetchReport);
</script>

<template>
  <div class="report-page">
    <section class="report-page__hero">
      <div>
        <p class="report-page__eyebrow">Order Report</p>
        <h2>订单统计</h2>
        <span>分析订单总量、有效订单和整体完成率，观察平台履约情况。</span>
      </div>

      <div class="report-page__toolbar">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          unlink-panels
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          :shortcuts="shortcuts"
        />
        <el-button type="primary" @click="fetchReport">查询</el-button>
      </div>
    </section>

    <section class="report-page__stats">
      <article class="stat-card">
        <span>订单总数</span>
        <strong>{{ totalOrderCount }}</strong>
      </article>
      <article class="stat-card">
        <span>有效订单</span>
        <strong>{{ validOrderCount }}</strong>
      </article>
      <article class="stat-card">
        <span>完成率</span>
        <strong>{{ (orderCompletionRate * 100).toFixed(2) }}%</strong>
      </article>
      <article class="stat-card">
        <span>无效订单</span>
        <strong>{{ invalidOrderCount }}</strong>
      </article>
    </section>

    <section class="report-page__panel">
      <div class="report-page__panel-head">
        <h3>订单趋势图</h3>
        <span>{{ dateRange?.[0] || '--' }} 至 {{ dateRange?.[1] || '--' }}</span>
      </div>
      <ChartPanel :option="chartOption" :loading="loading" />
    </section>
  </div>
</template>

<style scoped>
.report-page {
  display: grid;
  gap: 20px;
  padding: 28px;
  background: #f5f7fa;
}

.report-page__hero,
.report-page__panel,
.stat-card {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.06);
}

.report-page__hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 24px 28px;
}

.report-page__eyebrow {
  margin: 0 0 8px;
  color: #16a34a;
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.report-page__hero h2 {
  margin: 0 0 10px;
  font-size: 32px;
  color: #0f172a;
}

.report-page__hero span {
  color: #64748b;
  line-height: 1.7;
}

.report-page__toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.report-page__stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.stat-card {
  padding: 24px;
}

.stat-card span {
  display: block;
  color: #64748b;
  margin-bottom: 12px;
}

.stat-card strong {
  color: #0f172a;
  font-size: 30px;
}

.report-page__panel {
  padding: 24px;
}

.report-page__panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.report-page__panel-head h3 {
  margin: 0;
  font-size: 22px;
  color: #0f172a;
}

.report-page__panel-head span {
  color: #64748b;
}

@media (max-width: 1200px) {
  .report-page__hero,
  .report-page__panel-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .report-page__stats {
    grid-template-columns: 1fr;
  }

  .report-page__toolbar {
    width: 100%;
    flex-wrap: wrap;
  }
}
</style>
