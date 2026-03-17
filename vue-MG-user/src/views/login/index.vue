<script setup>
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

const loading = ref(false);
const formRef = ref();

const form = reactive({
  name: '',
  phone: '',
  password: ''
});

const rules = {
  name: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入登录密码', trigger: 'blur' }]
};

const handleLogin = async () => {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;

  loading.value = true;

  try {
    authStore.login({
      name: form.name,
      phone: form.phone
    });

    ElMessage.success('登录成功，欢迎回来');
    router.replace(route.query.redirect || '/user/home');
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="login-page">
    <div class="login-page__visual">
      <div class="login-page__badge">电脑端用户中心</div>
      <h1>补剂电商用户端</h1>
      <p>
        围绕“补剂分类 + 套餐分类 + 购物车结算”设计的桌面端体验，
        让浏览、选择规格和搭配购买都更顺手。
      </p>

      <div class="login-page__highlights">
        <article>
          <strong>分类导航</strong>
          <span>左侧固定栏快速切换补剂与套餐</span>
        </article>
        <article>
          <strong>规格选择</strong>
          <span>带规格补剂支持弹窗组合选择</span>
        </article>
        <article>
          <strong>购物车预留</strong>
          <span>为后续下单接口预留结算流程</span>
        </article>
      </div>
    </div>

    <div class="login-card">
      <div class="login-card__header">
        <p>Welcome Back</p>
        <h2>登录后进入首页选购</h2>
        <span>当前先使用前端登录成功态，后续可直接接用户登录接口。</span>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="用户名" prop="name">
          <el-input v-model="form.name" placeholder="例如：训练搭子Ares" size="large" />
        </el-form-item>

        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="选填，便于后续接真实用户资料" size="large" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            show-password
            placeholder="请输入密码"
            size="large"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-button class="login-card__button" :loading="loading" @click="handleLogin">
          登录并进入系统首页
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.2fr 0.88fr;
  gap: 28px;
  padding: 32px;
}

.login-page__visual,
.login-card {
  position: relative;
  overflow: hidden;
  border-radius: 34px;
  box-shadow: 0 24px 60px rgba(99, 60, 15, 0.16);
}

.login-page__visual {
  padding: 56px;
  background:
    radial-gradient(circle at top left, rgba(255, 211, 116, 0.7), transparent 30%),
    linear-gradient(145deg, rgba(53, 31, 14, 0.96), rgba(125, 72, 28, 0.9));
  color: #fff7ea;
}

.login-page__badge {
  display: inline-flex;
  padding: 10px 16px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.login-page__visual h1 {
  margin: 20px 0 14px;
  font-size: 54px;
  line-height: 1.05;
}

.login-page__visual > p {
  max-width: 560px;
  font-size: 18px;
  line-height: 1.8;
  color: rgba(255, 247, 234, 0.8);
}

.login-page__highlights {
  display: grid;
  gap: 18px;
  margin-top: 34px;
}

.login-page__highlights article {
  padding: 20px 22px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 24px;
}

.login-page__highlights strong {
  display: block;
  margin-bottom: 8px;
  font-size: 18px;
}

.login-page__highlights span {
  color: rgba(255, 247, 234, 0.76);
}

.login-card {
  align-self: center;
  padding: 42px;
  background: rgba(255, 250, 242, 0.88);
  backdrop-filter: blur(16px);
}

.login-card__header p {
  margin: 0 0 10px;
  font-size: 13px;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: #b47d42;
}

.login-card__header h2 {
  margin: 0 0 10px;
  font-size: 34px;
  color: #2f1d10;
}

.login-card__header span {
  display: inline-block;
  margin-bottom: 28px;
  line-height: 1.7;
  color: #7a5c41;
}

.login-card__button {
  width: 100%;
  height: 54px;
  margin-top: 6px;
  border: none;
  border-radius: 18px;
  background: linear-gradient(135deg, #ffcb58, #ff9228);
  color: #3d240f;
  font-size: 16px;
  font-weight: 700;
}

@media (max-width: 1180px) {
  .login-page {
    grid-template-columns: 1fr;
  }
}
</style>

