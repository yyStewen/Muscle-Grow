<script setup>
import { computed, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';

import ChartPanel from '@/components/report/ChartPanel.vue';
import { downloadBusinessReport, getUserStatistics } from '@/api/report';
import { buildDateShortcuts, getRecentDaysRange, parseCsvList, toNumber } from '@/utils/report';

const loading = ref(false);
const exportLoading = ref(false);
const dateRange = ref(getRecentDaysRange(7));
const shortcuts = buildDateShortcuts();
const dates = ref([]);
const totalUserList = ref([]);
const newUserList = ref([]);

const latestTotalUsers = computed(() =>
  totalUserList.value.length ? totalUserList.value[totalUserList.value.length - 1] : 0
);
const totalNewUsers = computed(() =>
  newUserList.value.reduce((sum, current) => sum + current, 0)
);
const averageNewUsers = computed(() =>
  newUserList.value.length ? totalNewUsers.value / newUserList.value.length : 0
);

const chartOption = computed(() => ({
  color: ['#409eff', '#67c23a'],
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
    name: '用户数',
    splitLine: {
      lineStyle: {
        color: '#e5e7eb'
      }
    }
  },
  series: [
    {
      name: '新增用户',
      type: 'bar',
      barMaxWidth: 26,
      data: newUserList.value,
      itemStyle: {
        borderRadius: [8, 8, 0, 0]
      }
    },
    {
      name: '累计用户',
      type: 'line',
      smooth: true,
      symbolSize: 8,
      data: totalUserList.value
    }
  ]
}));

const fetchReport = async () => {
  if (!dateRange.value?.length) {
    return;
  }

  loading.value = true;
  try {
    const res = await getUserStatistics({
      begin: dateRange.value[0],
      end: dateRange.value[1]
    });

    if (res.code === 1) {
      dates.value = parseCsvList(res.data?.dateList);
      totalUserList.value = parseCsvList(res.data?.totalUserList, toNumber);
      newUserList.value = parseCsvList(res.data?.newUserList, toNumber);
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
        <p class="report-page__eyebrow">User Report</p>
        <h2>用户统计</h2>
        <span>同时查看新增用户和累计用户变化，观察平台用户增长趋势。</span>
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
        <span>当前累计用户</span>
        <strong>{{ latestTotalUsers }}</strong>
      </article>
      <article class="stat-card">
        <span>区间新增用户</span>
        <strong>{{ totalNewUsers }}</strong>
      </article>
      <article class="stat-card">
        <span>日均新增用户</span>
        <strong>{{ averageNewUsers.toFixed(1) }}</strong>
      </article>
    </section>

    <section class="report-page__panel">
      <div class="report-page__panel-head">
        <h3>用户增长趋势图</h3>
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
  color: #409eff;
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
