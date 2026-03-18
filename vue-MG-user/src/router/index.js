import { createRouter, createWebHistory } from 'vue-router';

import LoginView from '@/views/login/index.vue';
import UserLayout from '@/views/layout/index.vue';
import HomeView from '@/views/home/index.vue';
import CartView from '@/views/cart/index.vue';
import AddressView from '@/views/address/index.vue';
import OrderCheckoutView from '@/views/order/checkout.vue';
import OrderListView from '@/views/order/index.vue';
import OrderDetailView from '@/views/order/detail.vue';
import { useAuthStore } from '@/stores/auth';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/user',
      component: UserLayout,
      redirect: '/user/home',
      children: [
        {
          path: 'home/:kind?/:categoryId?',
          name: 'userHome',
          component: HomeView
        },
        {
          path: 'cart',
          name: 'cart',
          component: CartView
        },
        {
          path: 'address',
          name: 'address',
          component: AddressView
        },
        {
          path: 'orders',
          name: 'userOrders',
          component: OrderListView
        },
        {
          path: 'orders/checkout',
          name: 'userOrderCheckout',
          component: OrderCheckoutView
        },
        {
          path: 'orders/:id',
          name: 'userOrderDetail',
          component: OrderDetailView
        }
      ]
    }
  ]
});

router.beforeEach((to) => {
  const authStore = useAuthStore();

  if (to.path === '/login' && authStore.isLoggedIn.value) {
    return '/user/home';
  }

  if (to.path !== '/login' && !authStore.isLoggedIn.value) {
    return {
      path: '/login',
      query: {
        redirect: to.fullPath
      }
    };
  }

  return true;
});

export default router;
