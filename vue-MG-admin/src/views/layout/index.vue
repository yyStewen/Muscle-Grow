<script setup>
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus';
import { useRouter } from 'vue-router';

import { updateCurrentEmployeePassword } from '@/api/employee';

const SOCKET_RECONNECT_DELAY = 3000;

const router = useRouter();
const loginName = ref('');
const passwordDialogVisible = ref(false);
const passwordSubmitting = ref(false);
const passwordFormRef = ref(null);

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const validateConfirmPassword = (_, value, callback) => {
  if (!value) {
    callback(new Error('请输入确认密码'));
    return;
  }

  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的新密码不一致'));
    return;
  }

  callback();
};

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码长度不能少于 6 位', trigger: 'blur' }
  ],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }]
};

let socket = null;
let reconnectTimer = null;
let manualClose = false;
let hasShownSocketError = false;

const getLoginUser = () => {
  try {
    return JSON.parse(localStorage.getItem('loginUser-Admin') || 'null');
  } catch (error) {
    return null;
  }
};

const syncLoginName = () => {
  const loginUser = getLoginUser();
  loginName.value = loginUser?.name || loginUser?.userName || loginUser?.username || '';
};

const getSocketSid = () => {
  const loginUser = getLoginUser();
  return String(loginUser?.id || loginUser?.userName || loginUser?.username || loginUser?.name || 'admin');
};

const getSocketUrl = () => {
  const rawBaseUrl = String(import.meta.env.VITE_APP_URL || '')
    .trim()
    .replace(/^['"]|['"]$/g, '');

  try {
    const targetUrl = rawBaseUrl ? new URL(rawBaseUrl) : new URL(window.location.origin);
    const socketProtocol = targetUrl.protocol === 'https:' ? 'wss:' : 'ws:';
    return `${socketProtocol}//${targetUrl.host}/ws/${encodeURIComponent(getSocketSid())}`;
  } catch (error) {
    const socketProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    return `${socketProtocol}//${window.location.host}/ws/${encodeURIComponent(getSocketSid())}`;
  }
};

const clearAdminLogin = () => {
  localStorage.removeItem('loginUser-Admin');
  localStorage.removeItem('loginUser');
};

const redirectToLogin = () => {
  clearAdminLogin();
  router.push('/login');
};

const showOrderNotification = (payload) => {
  const type = Number(payload?.type);
  const title = type === 1 ? '来单提醒' : type === 2 ? '用户催单' : '订单通知';
  const notificationType = type === 1 ? 'success' : 'warning';

  ElNotification({
    title,
    message: payload.content || '您有新的订单动态',
    type: notificationType,
    duration: 8000,
    offset: 72,
    onClick: () => {
      router.push({
        path: '/orders',
        query: {
          orderId: String(payload.orderId),
          noticeAt: String(Date.now())
        }
      });
    }
  });
};

const handleSocketMessage = (event) => {
  let payload;

  try {
    payload = JSON.parse(event.data);
  } catch (error) {
    return;
  }

  const type = Number(payload?.type);
  if (![1, 2].includes(type) || !payload.orderId || !payload.content) {
    return;
  }

  window.dispatchEvent(new CustomEvent('admin-order-notification', { detail: payload }));
  showOrderNotification(payload);
};

const scheduleReconnect = () => {
  if (manualClose) {
    return;
  }

  if (reconnectTimer) {
    window.clearTimeout(reconnectTimer);
  }

  reconnectTimer = window.setTimeout(() => {
    reconnectTimer = null;
    connectSocket();
  }, SOCKET_RECONNECT_DELAY);
};

const connectSocket = () => {
  if (typeof WebSocket === 'undefined') {
    ElMessage.warning('当前浏览器不支持 WebSocket，无法接收实时订单提醒');
    return;
  }

  if (socket && [WebSocket.OPEN, WebSocket.CONNECTING].includes(socket.readyState)) {
    return;
  }

  manualClose = false;
  socket = new WebSocket(getSocketUrl());

  socket.onopen = () => {
    hasShownSocketError = false;
  };

  socket.onmessage = (event) => {
    handleSocketMessage(event);
  };

  socket.onerror = () => {
    if (!hasShownSocketError) {
      hasShownSocketError = true;
      ElMessage.warning('实时提醒连接异常，系统将自动重连');
    }

    if (socket && socket.readyState !== WebSocket.CLOSED) {
      socket.close();
    }
  };

  socket.onclose = () => {
    socket = null;
    scheduleReconnect();
  };
};

const disconnectSocket = () => {
  manualClose = true;

  if (reconnectTimer) {
    window.clearTimeout(reconnectTimer);
    reconnectTimer = null;
  }

  if (socket && [WebSocket.OPEN, WebSocket.CONNECTING].includes(socket.readyState)) {
    socket.close();
  }

  socket = null;
};

const resetPasswordForm = () => {
  passwordForm.oldPassword = '';
  passwordForm.newPassword = '';
  passwordForm.confirmPassword = '';
  passwordFormRef.value?.clearValidate();
};

const openPasswordDialog = () => {
  resetPasswordForm();
  passwordDialogVisible.value = true;
};

const handlePasswordDialogClosed = () => {
  resetPasswordForm();
};

const forceLogoutAfterPasswordChange = () => {
  passwordDialogVisible.value = false;
  disconnectSocket();
  redirectToLogin();
};

const submitPasswordChange = async () => {
  if (!passwordFormRef.value) {
    return;
  }

  await passwordFormRef.value.validate();

  passwordSubmitting.value = true;
  try {
    const result = await updateCurrentEmployeePassword(passwordForm);
    if (result.code === 1) {
      ElMessage.success('密码修改成功，请重新登录');
      forceLogoutAfterPasswordChange();
    }
  } finally {
    passwordSubmitting.value = false;
  }
};

const logout = () => {
  ElMessageBox.confirm('确认退出登录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    disconnectSocket();
    clearAdminLogin();
    ElMessage.success('退出登录成功');
    router.push('/login');
  });
};

onMounted(() => {
  syncLoginName();
  connectSocket();
});

onBeforeUnmount(() => {
  disconnectSocket();
});
</script>

<template>
  <div class="common-layout">
    <el-container>
      <el-header class="header">
        <span class="title">Muscle-Grow 补剂商城管理端</span>
        <span class="right-tool">
          <a href="javascript:void(0)" @click="openPasswordDialog">
            <el-icon><EditPen /></el-icon> 修改密码
          </a>
          <span class="divider">|</span>
          <a href="javascript:void(0)" @click="logout">
            <el-icon><SwitchButton /></el-icon> 退出登录，{{ loginName || '管理员' }}
          </a>
        </span>
      </el-header>

      <el-container>
        <el-aside width="200px" class="aside">
          <el-menu router>
            <el-menu-item index="/index">
              <el-icon><Promotion /></el-icon> 工作台
            </el-menu-item>

            <el-menu-item index="/supplementCategory">
              <el-icon><Grid /></el-icon> 补剂分类管理
            </el-menu-item>

            <el-menu-item index="/supplement">
              <el-icon><Goods /></el-icon> 补剂管理
            </el-menu-item>

            <el-menu-item index="/setmeal">
              <el-icon><Handbag /></el-icon> 套餐管理
            </el-menu-item>

            <el-menu-item index="/orders">
              <el-icon><Document /></el-icon> 订单管理
            </el-menu-item>

            <el-menu-item index="/emp">
              <el-icon><User /></el-icon> 员工管理
            </el-menu-item>

            <el-menu-item index="/vouchers">
              <el-icon><Ticket /></el-icon> 优惠券管理
            </el-menu-item>

            <el-sub-menu index="/report">
              <template #title>
                <el-icon><Histogram /></el-icon>数据统计管理
              </template>
              <el-menu-item index="/incomeReport">
                <el-icon><InfoFilled /></el-icon>营业额统计
              </el-menu-item>
              <el-menu-item index="/userReport">
                <el-icon><Share /></el-icon>用户统计
              </el-menu-item>
              <el-menu-item index="/ordersReport">
                <el-icon><Document /></el-icon>订单统计
              </el-menu-item>
              <el-menu-item index="/soldTop10">
                <el-icon><Document /></el-icon>销量排名前十统计
              </el-menu-item>
            </el-sub-menu>
          </el-menu>
        </el-aside>

        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>

    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="460px"
      destroy-on-close
      @closed="handlePasswordDialogClosed"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="92px"
      >
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            show-password
            placeholder="请输入旧密码"
          />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
            placeholder="请输入新密码"
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入新密码"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="passwordDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="passwordSubmitting" @click="submitPasswordChange">
            保存修改
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.header {
  background-image: linear-gradient(to right, #00547d, #007fa4, #00aaa0, #00d072, #a8eb12);
}

.title {
  color: white;
  font-size: 40px;
  line-height: 60px;
  font-weight: 700;
}

.right-tool {
  float: right;
  display: flex;
  align-items: center;
  gap: 18px;
  line-height: 60px;
}

.divider {
  color: rgba(255, 255, 255, 0.75);
}

a {
  color: white;
  text-decoration: none;
}

.aside {
  width: 220px;
  border-right: 1px solid #ccc;
  height: 100vh;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
