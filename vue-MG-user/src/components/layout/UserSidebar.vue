<script setup>
defineProps({
  supplementCategories: {
    type: Array,
    default: () => []
  },
  setmealCategories: {
    type: Array,
    default: () => []
  },
  activeKind: {
    type: String,
    default: 'supplement'
  },
  activeCategoryId: {
    type: Number,
    default: 0
  }
});

defineEmits(['navigate']);
</script>

<template>
  <aside class="user-sidebar">
    <div class="user-sidebar__brand">
      <div class="user-sidebar__logo">MG</div>
      <div>
        <p>燃脂补给站</p>
        <strong>Muscle Grow</strong>
      </div>
    </div>

    <div class="user-sidebar__group">
      <p class="user-sidebar__title">补剂分类</p>
      <button
        v-for="item in supplementCategories"
        :key="`supplement-${item.id}`"
        type="button"
        class="user-sidebar__item"
        :class="{ 'is-active': activeKind === 'supplement' && activeCategoryId === item.id }"
        @click="$emit('navigate', { kind: 'supplement', id: item.id })"
      >
        <span>{{ item.name }}</span>
        <el-icon><ArrowRight /></el-icon>
      </button>
    </div>

    <div class="user-sidebar__group">
      <p class="user-sidebar__title">套餐分类</p>
      <button
        v-for="item in setmealCategories"
        :key="`setmeal-${item.id}`"
        type="button"
        class="user-sidebar__item"
        :class="{ 'is-active': activeKind === 'setmeal' && activeCategoryId === item.id }"
        @click="$emit('navigate', { kind: 'setmeal', id: item.id })"
      >
        <span>{{ item.name }}</span>
        <el-icon><ArrowRight /></el-icon>
      </button>
    </div>
  </aside>
</template>

<style scoped>
.user-sidebar {
  width: 260px;
  flex-shrink: 0;
  padding: 24px 18px;
  background: rgba(255, 250, 242, 0.85);
  border: 1px solid rgba(177, 137, 77, 0.15);
  border-radius: 30px;
  backdrop-filter: blur(18px);
  box-shadow: 0 20px 48px rgba(110, 76, 31, 0.1);
}

.user-sidebar__brand {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 28px;
  padding: 14px;
  background: linear-gradient(135deg, #fff8ee, #f6e6cf);
  border-radius: 22px;
}

.user-sidebar__logo {
  width: 54px;
  height: 54px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ffbf3a, #ff8e22);
  color: #43260f;
  font-weight: 800;
  font-size: 18px;
}

.user-sidebar__brand p {
  margin: 0 0 4px;
  color: #8c6b4d;
  font-size: 12px;
}

.user-sidebar__brand strong {
  font-size: 18px;
  color: #352114;
}

.user-sidebar__group + .user-sidebar__group {
  margin-top: 24px;
}

.user-sidebar__title {
  margin: 0 0 10px;
  padding-left: 8px;
  font-size: 12px;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  color: #97714e;
}

.user-sidebar__item {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  margin-bottom: 10px;
  background: transparent;
  border: none;
  border-radius: 18px;
  color: #5a4128;
  cursor: pointer;
  transition: transform 0.2s ease, background 0.2s ease, color 0.2s ease;
}

.user-sidebar__item:hover {
  transform: translateX(4px);
  background: #fff3df;
}

.user-sidebar__item.is-active {
  background: linear-gradient(135deg, #ffcf63, #ffae3d);
  color: #46280f;
  box-shadow: 0 14px 30px rgba(255, 175, 64, 0.3);
}

@media (max-width: 1100px) {
  .user-sidebar {
    width: 100%;
  }
}
</style>

