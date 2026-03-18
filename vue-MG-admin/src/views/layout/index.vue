<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus';
import { useRouter } from 'vue-router';

const SOCKET_RECONNECT_DELAY = 3000;

const router = useRouter();
const loginName = ref('');

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
  loginName.value = loginUser?.name || loginUser?.username || '';
};

const getSocketSid = () => {
  const loginUser = getLoginUser();
  return String(loginUser?.id || loginUser?.username || loginUser?.name || 'admin');
};

const getSocketUrl = () => {
  const rawBaseUrl = String(import.meta.env.VITE_APP_URL || '').trim().replace(/^['"]|['"]$/g, '');

  try {
    const targetUrl = rawBaseUrl ? new URL(rawBaseUrl) : new URL(window.location.origin);
    const socketProtocol = targetUrl.protocol === 'https:' ? 'wss:' : 'ws:';
    return `${socketProtocol}//${targetUrl.host}/ws/${encodeURIComponent(getSocketSid())}`;
  } catch (error) {
    const socketProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    return `${socketProtocol}//${window.location.host}/ws/${encodeURIComponent(getSocketSid())}`;
  }
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

const logout = () => {
  ElMessageBox.confirm('确认退出登录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    disconnectSocket();
    localStorage.removeItem('loginUser-Admin');
    localStorage.removeItem('loginUser');
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
        <span class="title">Muscle-Grow补剂商城管理端</span>
        <span class="right_tool">
          <a href="">
            <el-icon><EditPen /></el-icon> 修改密码 &nbsp;&nbsp;&nbsp; | &nbsp;&nbsp;&nbsp;
          </a>
          <a href="javascript:void(0)" @click="logout">
            <el-icon><SwitchButton /></el-icon> 退出登录【{{ loginName || '管理员' }}】
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
  </div>
</template>

<style scoped>
.header {
  background-image: linear-gradient(to right, #00547d, #007fa4, #00aaa0, #00d072, #a8eb12);
}

.title {
  color: white;
  font-size: 40px;
  font-family: 妤蜂綋;
  line-height: 60px;
  font-weight: bolder;
}

.right_tool {
  float: right;
  line-height: 60px;
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
</style>
