<script setup>
import * as echarts from 'echarts';
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';

const props = defineProps({
  option: {
    type: Object,
    default: () => ({})
  },
  loading: {
    type: Boolean,
    default: false
  },
  height: {
    type: String,
    default: '420px'
  }
});

const chartRef = ref(null);
let chartInstance = null;

const renderChart = async () => {
  await nextTick();

  if (!chartRef.value) {
    return;
  }

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value);
  }

  chartInstance.setOption(props.option || {}, true);
  chartInstance.resize();
};

const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize();
  }
};

watch(
  () => props.option,
  () => {
    renderChart();
  },
  { deep: true }
);

watch(
  () => props.loading,
  (value) => {
    if (!chartInstance) {
      return;
    }

    chartInstance[value ? 'showLoading' : 'hideLoading']({
      text: '加载中...'
    });
  }
);

onMounted(() => {
  renderChart();
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  if (chartInstance) {
    chartInstance.dispose();
    chartInstance = null;
  }
});
</script>

<template>
  <div ref="chartRef" class="chart-panel" :style="{ height }"></div>
</template>

<style scoped>
.chart-panel {
  width: 100%;
}
</style>
