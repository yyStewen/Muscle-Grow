<script setup>
defineProps({
  userName: {
    type: String,
    default: ''
  },
  cartCount: {
    type: Number,
    default: 0
  }
});

defineEmits(['go-cart', 'go-address', 'logout']);
</script>

<template>
  <header class="user-header">
    <div class="user-header__headline">
      <p class="user-header__eyebrow">Muscle Grow Fuel Lab</p>
      <h1>Your nutrition flow is ready for training day.</h1>
    </div>

    <div class="user-header__actions">
      <div class="user-header__user">
        <div class="user-header__avatar">{{ userName.slice(0, 1) || 'U' }}</div>
        <div>
          <p class="user-header__label">Current User</p>
          <strong>{{ userName || 'Gym Partner' }}</strong>
        </div>
      </div>

      <el-badge :value="cartCount" :hidden="!cartCount" class="user-header__cart-badge">
        <el-button class="user-header__cart-btn" @click="$emit('go-cart')">
          <el-icon><ShoppingCart /></el-icon>
          Cart
        </el-button>
      </el-badge>

      <el-button class="user-header__address" @click="$emit('go-address')">
        <el-icon><Location /></el-icon>
        Address Book
      </el-button>

      <el-button class="user-header__logout" @click="$emit('logout')">Logout</el-button>
    </div>
  </header>
</template>

<style scoped>
.user-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 28px 32px;
  background:
    radial-gradient(circle at top left, rgba(255, 208, 90, 0.55), transparent 32%),
    linear-gradient(135deg, rgba(62, 36, 12, 0.96), rgba(91, 50, 17, 0.92));
  color: #fff7eb;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 28px;
  box-shadow: 0 24px 60px rgba(70, 36, 4, 0.28);
}

.user-header__headline {
  max-width: 620px;
}

.user-header__eyebrow {
  margin: 0 0 10px;
  font-size: 12px;
  letter-spacing: 0.26em;
  text-transform: uppercase;
  color: rgba(255, 241, 220, 0.72);
}

.user-header h1 {
  margin: 0;
  font-size: 30px;
  line-height: 1.2;
  font-weight: 700;
}

.user-header__actions {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-left: auto;
}

.user-header__user {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 188px;
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 20px;
  backdrop-filter: blur(14px);
}

.user-header__avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ffca45, #ff9827);
  color: #41230d;
  font-weight: 700;
}

.user-header__label {
  margin: 0 0 4px;
  font-size: 12px;
  color: rgba(255, 247, 235, 0.76);
}

.user-header__cart-btn,
.user-header__address,
.user-header__logout {
  height: 48px;
  padding: 0 20px;
  border: none;
  border-radius: 18px;
}

.user-header__cart-btn {
  background: #ffd256;
  color: #3f250f;
  font-weight: 700;
}

.user-header__address {
  background: rgba(255, 255, 255, 0.16);
  color: #fff7eb;
}

.user-header__logout {
  background: rgba(255, 255, 255, 0.12);
  color: #fff7eb;
}

@media (max-width: 1280px) {
  .user-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .user-header__actions {
    width: 100%;
    flex-wrap: wrap;
  }
}
</style>
