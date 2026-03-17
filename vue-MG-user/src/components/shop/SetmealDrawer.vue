<script setup>
defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  setmeal: {
    type: Object,
    default: null
  },
  items: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['update:modelValue', 'add']);
</script>

<template>
  <el-drawer
    :model-value="modelValue"
    size="440px"
    direction="rtl"
    @close="$emit('update:modelValue', false)"
  >
    <template #header>
      <div class="setmeal-drawer__header">
        <img :src="setmeal?.image" :alt="setmeal?.name" class="setmeal-drawer__cover" />
        <div>
          <p class="setmeal-drawer__eyebrow">套餐详情</p>
          <h3>{{ setmeal?.name }}</h3>
          <p class="setmeal-drawer__desc">{{ setmeal?.description || '高效搭配，一次配齐训练补给。' }}</p>
        </div>
      </div>
    </template>

    <div v-loading="loading" class="setmeal-drawer__content">
      <div v-if="items.length" class="setmeal-drawer__list">
        <div v-for="item in items" :key="`${item.name}-${item.copies}`" class="setmeal-drawer__item">
          <img :src="item.image" :alt="item.name" />
          <div>
            <strong>{{ item.name }}</strong>
            <p>{{ item.description || '推荐搭配补剂' }}</p>
          </div>
          <span>x{{ item.copies }}</span>
        </div>
      </div>
      <el-empty v-else description="暂无补剂明细" />
    </div>

    <template #footer>
      <div class="setmeal-drawer__footer">
        <el-button @click="$emit('update:modelValue', false)">关闭</el-button>
        <el-button type="warning" @click="$emit('add')">加入购物车</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<style scoped>
.setmeal-drawer__header {
  display: flex;
  gap: 16px;
}

.setmeal-drawer__cover {
  width: 108px;
  height: 108px;
  border-radius: 26px;
  object-fit: cover;
  background: #fff4e3;
}

.setmeal-drawer__eyebrow {
  margin: 0 0 8px;
  font-size: 12px;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  color: #af7c41;
}

.setmeal-drawer__header h3 {
  margin: 0 0 10px;
  font-size: 24px;
  color: #321f11;
}

.setmeal-drawer__desc {
  margin: 0;
  color: #836345;
  line-height: 1.6;
}

.setmeal-drawer__list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.setmeal-drawer__item {
  display: grid;
  grid-template-columns: 74px 1fr auto;
  gap: 14px;
  align-items: center;
  padding: 12px;
  background: #fff8ef;
  border-radius: 18px;
}

.setmeal-drawer__item img {
  width: 74px;
  height: 74px;
  object-fit: cover;
  border-radius: 18px;
}

.setmeal-drawer__item strong {
  color: #352114;
}

.setmeal-drawer__item p {
  margin: 6px 0 0;
  color: #8f6c47;
  font-size: 13px;
  line-height: 1.5;
}

.setmeal-drawer__item span {
  color: #ff8a1d;
  font-weight: 700;
}

.setmeal-drawer__footer {
  display: flex;
  justify-content: flex-end;
}
</style>

