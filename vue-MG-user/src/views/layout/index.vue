<script setup>
import { computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { logoutUser } from '@/api/auth';
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
          @logout="handleLogout"
        />

        <main class="user-shell__page">
          <router-view />
        </main>
      </div>
    </div>
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

@media (max-width: 1100px) {
  .user-shell__layout {
    flex-direction: column;
  }
}
</style>
