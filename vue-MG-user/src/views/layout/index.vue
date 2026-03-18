<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { useRoute, useRouter } from 'vue-router';

import { logoutUser, updateUserPassword } from '@/api/auth';
import UserHeader from '@/components/layout/UserHeader.vue';
import UserSidebar from '@/components/layout/UserSidebar.vue';
import { useAuthStore } from '@/stores/auth';
import { useCartStore } from '@/stores/cart';
import { useCatalogStore } from '@/stores/catalog';

const router = useRouter();
const route = useRoute();

const authStore = useAuthStore();
const cartStore = useCartStore();
const catalogStore = useCatalogStore();
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
    callback(new Error('Please confirm your new password'));
    return;
  }

  if (value !== passwordForm.newPassword) {
    callback(new Error('The two new passwords do not match'));
    return;
  }

  callback();
};

const passwordRules = {
  oldPassword: [{ required: true, message: 'Please enter your current password', trigger: 'blur' }],
  newPassword: [
    { required: true, message: 'Please enter a new password', trigger: 'blur' },
    { min: 6, message: 'Password must be at least 6 characters', trigger: 'blur' }
  ],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }]
};

const activeKind = computed(() => (route.params.kind === 'setmeal' ? 'setmeal' : 'supplement'));
const activeCategoryId = computed(() => Number(route.params.categoryId || 0));

const resolveDefaultCategory = () => {
  const supplementCategory = catalogStore.state.supplementCategories[0];
  const setmealCategory = catalogStore.state.setmealCategories[0];

  if (supplementCategory) {
    return {
      name: 'userHome',
      params: {
        kind: 'supplement',
        categoryId: String(supplementCategory.id)
      }
    };
  }

  if (setmealCategory) {
    return {
      name: 'userHome',
      params: {
        kind: 'setmeal',
        categoryId: String(setmealCategory.id)
      }
    };
  }

  return null;
};

const ensureDefaultRoute = () => {
  if (route.name !== 'userHome') return;
  if (route.params.kind && route.params.categoryId) return;

  const fallback = resolveDefaultCategory();
  if (fallback) {
    router.replace(fallback);
  }
};

const handleNavigate = ({ kind, id }) => {
  router.push({
    name: 'userHome',
    params: {
      kind,
      categoryId: String(id)
    }
  });
};

const handleLogout = async () => {
  try {
    await logoutUser();
  } catch (error) {
    // Ignore logout request failures and always clear local session.
  }

  authStore.logout();
  cartStore.reset();
  router.replace('/login');
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

const logoutAfterPasswordChange = () => {
  passwordDialogVisible.value = false;
  authStore.logout();
  cartStore.reset();
  router.replace('/login');
};

const submitPasswordChange = async () => {
  if (!passwordFormRef.value) {
    return;
  }

  await passwordFormRef.value.validate();

  passwordSubmitting.value = true;
  try {
    const res = await updateUserPassword(passwordForm);
    if (res.code === 1) {
      ElMessage.success('Password updated successfully. Please log in again.');
      logoutAfterPasswordChange();
    }
  } finally {
    passwordSubmitting.value = false;
  }
};

onMounted(async () => {
  await Promise.allSettled([
    catalogStore.fetchCatalogCategories(),
    cartStore.fetchCart()
  ]);
  ensureDefaultRoute();
});

watch(
  () => [
    catalogStore.state.supplementCategories.length,
    catalogStore.state.setmealCategories.length,
    route.fullPath
  ],
  () => {
    ensureDefaultRoute();
  }
);
</script>

<template>
  <div class="user-shell">
    <div class="user-shell__glow user-shell__glow--left"></div>
    <div class="user-shell__glow user-shell__glow--right"></div>

    <div class="user-shell__layout">
      <UserSidebar
        :supplement-categories="catalogStore.state.supplementCategories"
        :setmeal-categories="catalogStore.state.setmealCategories"
        :active-kind="activeKind"
        :active-category-id="activeCategoryId"
        @navigate="handleNavigate"
      />

      <div class="user-shell__main">
        <UserHeader
          :user-name="authStore.state.profile?.name"
          :cart-count="cartStore.totalCount.value"
          @go-cart="router.push('/user/cart')"
          @go-address="router.push('/user/address')"
          @go-orders="router.push('/user/orders')"
          @change-password="openPasswordDialog"
          @logout="handleLogout"
        />

        <main class="user-shell__page">
          <router-view />
        </main>
      </div>
    </div>

    <el-dialog
      v-model="passwordDialogVisible"
      title="Change Password"
      width="460px"
      destroy-on-close
      @closed="handlePasswordDialogClosed"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="128px"
      >
        <el-form-item label="Current Password" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            show-password
            placeholder="Enter your current password"
          />
        </el-form-item>

        <el-form-item label="New Password" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
            placeholder="Enter a new password"
          />
        </el-form-item>

        <el-form-item label="Confirm Password" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
            placeholder="Enter the password again"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="user-shell__dialog-footer">
          <el-button @click="passwordDialogVisible = false">Cancel</el-button>
          <el-button type="primary" :loading="passwordSubmitting" @click="submitPasswordChange">
            Save
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.user-shell {
  position: relative;
  min-height: 100vh;
  padding: 24px;
  overflow: hidden;
}

.user-shell__glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(18px);
  opacity: 0.42;
}

.user-shell__glow--left {
  inset: -120px auto auto -80px;
  width: 320px;
  height: 320px;
  background: rgba(255, 199, 87, 0.45);
}

.user-shell__glow--right {
  inset: auto -80px 80px auto;
  width: 300px;
  height: 300px;
  background: rgba(255, 150, 65, 0.26);
}

.user-shell__layout {
  position: relative;
  z-index: 1;
  display: flex;
  gap: 22px;
  align-items: flex-start;
}

.user-shell__main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.user-shell__page {
  min-height: calc(100vh - 160px);
}

.user-shell__dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 1100px) {
  .user-shell__layout {
    flex-direction: column;
  }
}
</style>
