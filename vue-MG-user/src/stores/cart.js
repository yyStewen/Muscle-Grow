import { computed, reactive } from 'vue';

import {
  addCartItem,
  cleanCart,
  getCartList,
  subCartItem
} from '@/api/cart';
import { buildSpecText } from '@/utils/shop';

const state = reactive({
  items: [],
  loading: false,
  syncing: false
});

const mapCartItem = (item) => ({
  id: item.id,
  key: item.id,
  type: item.setmealId ? 'setmeal' : 'supplement',
  productId: item.setmealId || item.supplementId,
  supplementId: item.supplementId || null,
  setmealId: item.setmealId || null,
  name: item.name,
  image: item.image,
  price: Number(item.amount || 0),
  quantity: Number(item.number || 0),
  specText: item.supplementDetail || '',
  description: '',
  createTime: item.createTime
});

const setCartItems = (items = []) => {
  state.items = items.map(mapCartItem);
};

const reset = () => {
  state.items = [];
  state.loading = false;
  state.syncing = false;
};

const fetchCart = async () => {
  state.loading = true;

  try {
    const res = await getCartList();
    if (res.code === 1) {
      setCartItems(res.data || []);
      return state.items;
    }

    return [];
  } finally {
    state.loading = false;
  }
};

const withSync = async (handler) => {
  state.syncing = true;

  try {
    return await handler();
  } finally {
    state.syncing = false;
  }
};

const buildPayloadFromItem = (item) => ({
  supplementId: item.supplementId || undefined,
  setmealId: item.setmealId || undefined,
  supplementDetail: item.specText || undefined
});

const addSupplement = async (product, selectedSpecs = {}) =>
  withSync(async () => {
    const res = await addCartItem({
      supplementId: product.id,
      supplementDetail: buildSpecText(selectedSpecs) || undefined
    });

    if (res.code === 1) {
      await fetchCart();
      return true;
    }

    return false;
  });

const addSetmeal = async (setmeal) =>
  withSync(async () => {
    const res = await addCartItem({
      setmealId: setmeal.id
    });

    if (res.code === 1) {
      await fetchCart();
      return true;
    }

    return false;
  });

const increase = async (item) =>
  withSync(async () => {
    const res = await addCartItem(buildPayloadFromItem(item));
    if (res.code === 1) {
      await fetchCart();
      return true;
    }

    return false;
  });

const decrease = async (item) =>
  withSync(async () => {
    const res = await subCartItem(buildPayloadFromItem(item));
    if (res.code === 1) {
      await fetchCart();
      return true;
    }

    return false;
  });

const remove = async (item) =>
  withSync(async () => {
    for (let i = 0; i < Number(item.quantity || 0); i += 1) {
      const res = await subCartItem(buildPayloadFromItem(item));
      if (res.code !== 1) {
        return false;
      }
    }

    await fetchCart();
    return true;
  });

const clear = async () =>
  withSync(async () => {
    const res = await cleanCart();
    if (res.code === 1) {
      state.items = [];
      return true;
    }

    return false;
  });

const totalCount = computed(() =>
  state.items.reduce((total, item) => total + item.quantity, 0)
);

const totalAmount = computed(() =>
  state.items.reduce((total, item) => total + item.quantity * Number(item.price || 0), 0)
);

export const useCartStore = () => ({
  state,
  totalCount,
  totalAmount,
  fetchCart,
  addSupplement,
  addSetmeal,
  increase,
  decrease,
  remove,
  clear,
  reset
});
