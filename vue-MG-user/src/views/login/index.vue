<script setup>
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

import { loginUser, registerUser } from '@/api/auth';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

const activeMode = ref('login');
const submitting = ref(false);

const loginFormRef = ref();
const registerFormRef = ref();

const loginForm = reactive({
  phone: '',
  password: ''
});

const registerForm = reactive({
  name: '',
  phone: '',
  password: '',
  confirmPassword: ''
});

const validatePhone = (_, value, callback) => {
  if (!value) {
    callback(new Error('Please enter your phone number'));
    return;
  }

  if (!/^1\d{10}$/.test(value)) {
    callback(new Error('Please enter a valid 11-digit phone number'));
    return;
  }

  callback();
};

const validatePassword = (_, value, callback) => {
  if (!value) {
    callback(new Error('Please enter your password'));
    return;
  }

  if (value.length < 6) {
    callback(new Error('Password must be at least 6 characters'));
    return;
  }

  callback();
};

const validateConfirmPassword = (_, value, callback) => {
  if (!value) {
    callback(new Error('Please confirm your password'));
    return;
  }

  if (value !== registerForm.password) {
    callback(new Error('The two passwords do not match'));
    return;
  }

  callback();
};

const loginRules = {
  phone: [{ validator: validatePhone, trigger: 'blur' }],
  password: [{ validator: validatePassword, trigger: 'blur' }]
};

const registerRules = {
  name: [{ required: true, message: 'Please enter your display name', trigger: 'blur' }],
  phone: [{ validator: validatePhone, trigger: 'blur' }],
  password: [{ validator: validatePassword, trigger: 'blur' }],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }]
};

const switchMode = (mode) => {
  activeMode.value = mode;
};

const handleLogin = async () => {
  const valid = await loginFormRef.value?.validate().then(() => true).catch(() => false);
  if (!valid) return;

  submitting.value = true;

  try {
    const res = await loginUser({
      phone: loginForm.phone,
      password: loginForm.password
    });

    if (res.code !== 1) {
      ElMessage.error(res.msg || 'Login failed. Please check your phone and password.');
      return;
    }

    authStore.login(res.data || {});
    ElMessage.success('Login successful');
    router.replace(route.query.redirect || '/user/home');
  } finally {
    submitting.value = false;
  }
};

const handleRegister = async () => {
  const valid = await registerFormRef.value?.validate().then(() => true).catch(() => false);
  if (!valid) return;

  submitting.value = true;

  try {
    const res = await registerUser({
      name: registerForm.name,
      phone: registerForm.phone,
      password: registerForm.password,
      confirmPassword: registerForm.confirmPassword
    });

    if (res.code !== 1) {
      ElMessage.error(res.msg || 'Registration failed. Please try again.');
      return;
    }

    loginForm.phone = registerForm.phone;
    loginForm.password = '';
    registerForm.password = '';
    registerForm.confirmPassword = '';
    activeMode.value = 'login';

    ElMessage.success('Registration successful. Please sign in.');
  } finally {
    submitting.value = false;
  }
};
</script>

<template>
  <div class="auth-page">
    <section class="auth-visual">
      <div class="auth-visual__badge">Muscle Grow User</div>
      <h1>Desktop User Portal</h1>
      <p>
        Sign in to browse supplements and setmeals by category, choose product specifications,
        and open the cart page that is ready for the next checkout step.
      </p>

      <div class="auth-visual__cards">
        <article>
          <strong>Category Navigation</strong>
          <span>Use the left sidebar to switch between supplement and setmeal categories.</span>
        </article>
        <article>
          <strong>Specification Flow</strong>
          <span>Products with details show a specification action before they are added to cart.</span>
        </article>
        <article>
          <strong>Cart Ready</strong>
          <span>The cart entry in the top-right corner is ready for later order and checkout work.</span>
        </article>
      </div>
    </section>

    <section class="auth-panel">
      <div class="auth-panel__header">
        <p>Fuel Your Training</p>
        <h2>{{ activeMode === 'login' ? 'Account Login' : 'Create Account' }}</h2>
        <span>
          {{ activeMode === 'login'
            ? 'Use your phone number and password to enter the user storefront.'
            : 'Create a user account first, then sign in with the same credentials.' }}
        </span>
      </div>

      <div class="auth-switcher">
        <button
          type="button"
          :class="['auth-switcher__item', { 'is-active': activeMode === 'login' }]"
          @click="switchMode('login')"
        >
          Login
        </button>
        <button
          type="button"
          :class="['auth-switcher__item', { 'is-active': activeMode === 'register' }]"
          @click="switchMode('register')"
        >
          Register
        </button>
      </div>

      <el-form
        v-if="activeMode === 'login'"
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-position="top"
        class="auth-form"
      >
        <el-form-item label="Phone" prop="phone">
          <el-input
            v-model="loginForm.phone"
            placeholder="Enter your 11-digit phone number"
            size="large"
            maxlength="11"
          />
        </el-form-item>

        <el-form-item label="Password" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            show-password
            placeholder="Enter your password"
            size="large"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-button class="auth-form__submit" :loading="submitting" @click="handleLogin">
          Login and Enter Home
        </el-button>
      </el-form>

      <el-form
        v-else
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-position="top"
        class="auth-form"
      >
        <el-form-item label="Display Name" prop="name">
          <el-input
            v-model="registerForm.name"
            placeholder="Example: Training Partner Ares"
            size="large"
            maxlength="20"
          />
        </el-form-item>

        <el-form-item label="Phone" prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="Enter your 11-digit phone number"
            size="large"
            maxlength="11"
          />
        </el-form-item>

        <el-form-item label="Password" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            show-password
            placeholder="At least 6 characters"
            size="large"
          />
        </el-form-item>

        <el-form-item label="Confirm Password" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            show-password
            placeholder="Enter the password again"
            size="large"
            @keyup.enter="handleRegister"
          />
        </el-form-item>

        <el-button class="auth-form__submit" :loading="submitting" @click="handleRegister">
          Create Account
        </el-button>
      </el-form>
    </section>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.18fr 0.92fr;
  gap: 28px;
  padding: 32px;
}

.auth-visual,
.auth-panel {
  position: relative;
  overflow: hidden;
  border-radius: 34px;
  box-shadow: 0 24px 60px rgba(93, 55, 12, 0.16);
}

.auth-visual {
  padding: 56px;
  background:
    radial-gradient(circle at top left, rgba(255, 211, 116, 0.72), transparent 30%),
    linear-gradient(145deg, rgba(53, 31, 14, 0.96), rgba(125, 72, 28, 0.92));
  color: #fff7ea;
}

.auth-visual__badge {
  display: inline-flex;
  padding: 10px 16px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.auth-visual h1 {
  margin: 20px 0 14px;
  font-size: 54px;
  line-height: 1.05;
}

.auth-visual > p {
  max-width: 560px;
  font-size: 18px;
  line-height: 1.8;
  color: rgba(255, 247, 234, 0.84);
}

.auth-visual__cards {
  display: grid;
  gap: 18px;
  margin-top: 34px;
}

.auth-visual__cards article {
  padding: 20px 22px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 24px;
}

.auth-visual__cards strong {
  display: block;
  margin-bottom: 8px;
  font-size: 18px;
}

.auth-visual__cards span {
  color: rgba(255, 247, 234, 0.76);
  line-height: 1.7;
}

.auth-panel {
  align-self: center;
  padding: 42px;
  background: rgba(255, 250, 242, 0.9);
  backdrop-filter: blur(16px);
}

.auth-panel__header p {
  margin: 0 0 10px;
  font-size: 13px;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: #b47d42;
}

.auth-panel__header h2 {
  margin: 0 0 10px;
  font-size: 34px;
  color: #2f1d10;
}

.auth-panel__header span {
  display: inline-block;
  margin-bottom: 26px;
  line-height: 1.7;
  color: #7a5c41;
}

.auth-switcher {
  display: inline-grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 6px;
  width: 100%;
  margin-bottom: 24px;
  padding: 6px;
  background: #fff2df;
  border-radius: 20px;
}

.auth-switcher__item {
  height: 46px;
  border: none;
  border-radius: 16px;
  background: transparent;
  color: #8a633e;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.auth-switcher__item.is-active {
  background: linear-gradient(135deg, #ffcb58, #ff9228);
  color: #3d240f;
  box-shadow: 0 12px 24px rgba(255, 154, 41, 0.18);
}

.auth-form :deep(.el-form-item__label) {
  color: #4a301b;
  font-weight: 700;
}

.auth-form__submit {
  width: 100%;
  height: 54px;
  margin-top: 8px;
  border: none;
  border-radius: 18px;
  background: linear-gradient(135deg, #ffcb58, #ff9228);
  color: #3d240f;
  font-size: 16px;
  font-weight: 700;
}

@media (max-width: 1180px) {
  .auth-page {
    grid-template-columns: 1fr;
  }
}
</style>
