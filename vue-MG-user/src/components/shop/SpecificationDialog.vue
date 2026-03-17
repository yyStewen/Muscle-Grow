<script setup>
import { computed, reactive, watch } from 'vue';

import { formatCurrency } from '@/utils/shop';

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  product: {
    type: Object,
    default: null
  }
});

const emit = defineEmits(['update:modelValue', 'confirm']);

const selection = reactive({});

const detailGroups = computed(() => props.product?.detailGroups || []);
const canSubmit = computed(() =>
  detailGroups.value.every((group) => selection[group.name])
);

const initSelection = () => {
  Object.keys(selection).forEach((key) => delete selection[key]);
  detailGroups.value.forEach((group) => {
    selection[group.name] = group.values[0] || '';
  });
};

watch(
  () => [props.modelValue, props.product?.id],
  ([visible]) => {
    if (visible) {
      initSelection();
    }
  },
  { immediate: true }
);

const closeDialog = () => {
  emit('update:modelValue', false);
};

const handleConfirm = () => {
  if (!canSubmit.value) return;
  emit('confirm', { ...selection });
  closeDialog();
};
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    width="560px"
    class="spec-dialog"
    @close="closeDialog"
  >
    <template #header>
      <div class="spec-dialog__header">
        <img :src="product?.image" :alt="product?.name" class="spec-dialog__image" />
        <div>
          <p class="spec-dialog__eyebrow">规格选择</p>
          <h3>{{ product?.name }}</h3>
          <p class="spec-dialog__price">¥{{ formatCurrency(product?.price) }}</p>
        </div>
      </div>
    </template>

    <div class="spec-dialog__content">
      <div v-for="group in detailGroups" :key="group.id || group.name" class="spec-dialog__group">
        <p>{{ group.name }}</p>
        <el-radio-group v-model="selection[group.name]" class="spec-dialog__options">
          <el-radio-button
            v-for="value in group.values"
            :key="value"
            :label="value"
          />
        </el-radio-group>
      </div>
    </div>

    <template #footer>
      <div class="spec-dialog__footer">
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="warning" :disabled="!canSubmit" @click="handleConfirm">
          加入购物车
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.spec-dialog__header {
  display: flex;
  align-items: center;
  gap: 18px;
}

.spec-dialog__image {
  width: 92px;
  height: 92px;
  border-radius: 22px;
  object-fit: cover;
  background: #fff3df;
}

.spec-dialog__eyebrow {
  margin: 0 0 8px;
  font-size: 12px;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  color: #aa7a46;
}

.spec-dialog__header h3 {
  margin: 0 0 8px;
  font-size: 24px;
  color: #2f1d10;
}

.spec-dialog__price {
  margin: 0;
  font-size: 22px;
  color: #ff7d1c;
  font-weight: 700;
}

.spec-dialog__group + .spec-dialog__group {
  margin-top: 22px;
}

.spec-dialog__group p {
  margin: 0 0 12px;
  color: #654a2c;
  font-weight: 600;
}

.spec-dialog__options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.spec-dialog__footer {
  display: flex;
  justify-content: flex-end;
}
</style>

