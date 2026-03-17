import { computed, reactive } from 'vue';

import { buildSpecText } from '@/utils/shop';

const STORAGE_KEY = 'mg-user-cart';

const loadItems = () => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : [];
  } catch (error) {
    return [];
  }
};

const state = reactive({
  items: loadItems()
});

const persist = () => {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(state.items));
};

const upsertItem = (payload) => {
  const existing = state.items.find((item) => item.key === payload.key);

  if (existing) {
    existing.quantity += payload.quantity || 1;
  } else {
    state.items.unshift({
      ...payload,
      quantity: payload.quantity || 1
    });
  }

  persist();
};

const addSupplement = (product, selectedSpecs = {}) => {
  const specText = buildSpecText(selectedSpecs);
  upsertItem({
    key: `supplement-${product.id}-${specText || 'default'}`,
    productId: product.id,
    type: 'supplement',
    name: product.name,
    image: product.image,
    price: Number(product.price || 0),
    description: product.description,
    specText,
    specs: selectedSpecs
  });
};

const addSetmeal = (setmeal) => {
  upsertItem({
    key: `setmeal-${setmeal.id}`,
    productId: setmeal.id,
    type: 'setmeal',
    name: setmeal.name,
    image: setmeal.image,
    price: Number(setmeal.price || 0),
    description: setmeal.description,
    specText: '套餐'
  });
};

const increase = (key) => {
  const item = state.items.find((entry) => entry.key === key);
  if (!item) return;
  item.quantity += 1;
  persist();
};

const decrease = (key) => {
  const item = state.items.find((entry) => entry.key === key);
  if (!item) return;

  if (item.quantity <= 1) {
    state.items = state.items.filter((entry) => entry.key !== key);
  } else {
    item.quantity -= 1;
  }

  persist();
};

const remove = (key) => {
  state.items = state.items.filter((entry) => entry.key !== key);
  persist();
};

const clear = () => {
  state.items = [];
  persist();
};

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
  addSupplement,
  addSetmeal,
  increase,
  decrease,
  remove,
  clear
});

