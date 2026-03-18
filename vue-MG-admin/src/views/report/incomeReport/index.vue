<script setup>
import { computed, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';

import ChartPanel from '@/components/report/ChartPanel.vue';
import { downloadBusinessReport, getTurnoverStatistics } from '@/api/report';
import { buildDateShortcuts, getRecentDaysRange, parseCsvList, toNumber } from '@/utils/report';

const loading = ref(false);
const exportLoading = ref(false);
const dateRange = ref(getRecentDaysRange(7));
const shortcuts = buildDateShortcuts();
const dates = ref([]);
const turnoverList = ref([]);

const totalTurnover = computed(() =>
  turnoverList.value.reduce((sum, current) => sum + current, 0)
);
const averageTurnover = computed(() =>
  turnoverList.value.length ? totalTurnover.value / turnoverList.value.length : 0
);
const maxTurnover = computed(() =>
  turnoverList.value.length ? Math.max(...turnoverList.value) : 0
);

const formatCurrency = (value) => `\u00A5${Number(value || 0).toFixed(2)}`;

const chartOption = computed(() => ({
  color: ['#ff7a45'],
  tooltip: {
    trigger: 'axis'
  },
  grid: {
    left: 60,
    right: 32,
    top: 56,
    bottom: 48
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: dates.value,
    axisLine: {
      lineStyle: {
        color: '#cbd5e1'
      }
    }
  },
  yAxis: {
    type: 'value',
    name: '营业额',
    axisLabel: {
      formatter: '{value}'
    },
    splitLine: {
      lineStyle: {
        color: '#e5e7eb'
      }
    }
  },
  series: [
    {
      name: '营业额',
      type: 'line',
      smooth: true,
      symbolSize: 8,
      data: turnoverList.value,
      areaStyle: {
        color: 'rgba(255, 122, 69, 0.18)'
      },
      lineStyle: {
        width: 3
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
    const res = await getTurnoverStatistics({
      begin: dateRange.value[0],
      end: dateRange.value[1]
    });

    if (res.code === 1) {
      dates.value = parseCsvList(res.data?.dateList);
      turnoverList.value = parseCsvList(res.data?.turnoverList, toNumber);
    }
  } finally {
    loading.value = false;
  }
};

const handleExport = async () => {
  exportLoading.value = true;
  try {
    await downloadBusinessReport();
    ElMessage.success('运营报表已开始下载');
  } finally {
    exportLoading.value = false;
  }
};

onMounted(fetchReport);
</script>

<template>
  <div class="report-page">
    <section class="report-page__hero">
      <div>
        <p class="report-page__eyebrow">Turnover Report</p>
        <h2>营业额统计</h2>
        <span>查看时间范围内的每日营业额走势，快速判断店铺收入变化。</span>
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
        <el-button type="success" plain :loading="exportLoading" @click="handleExport">
          导出近30日运营报表
        </el-button>
      </div>
    </section>

    <section class="report-page__stats">
      <article class="stat-card">
        <span>累计营业额</span>
        <strong>{{ formatCurrency(totalTurnover) }}</strong>
      </article>
      <article class="stat-card">
        <span>日均营业额</span>
        <strong>{{ formatCurrency(averageTurnover) }}</strong>
      </article>
      <article class="stat-card">
        <span>最高单日营业额</span>
        <strong>{{ formatCurrency(maxTurnover) }}</strong>
      </article>
    </section>

    <section class="report-page__panel">
      <div class="report-page__panel-head">
        <h3>营业额趋势图</h3>
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
  color: #f97316;
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
  grid-template-columns: repeat(3, minmax(0, 1fr));
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
