import { reactive } from 'vue';

import { getCategoryList } from '@/api/shop';

const state = reactive({
  supplementCategories: [],
  setmealCategories: [],
  loading: false
});

export const fetchCatalogCategories = async () => {
  if (state.loading) return;

  state.loading = true;

  try {
    const [supplementRes, setmealRes] = await Promise.all([
      getCategoryList(1),
      getCategoryList(2)
    ]);

    if (supplementRes.code === 1) {
      state.supplementCategories = supplementRes.data || [];
    }

    if (setmealRes.code === 1) {
      state.setmealCategories = setmealRes.data || [];
    }
  } finally {
    state.loading = false;
  }
};

export const useCatalogStore = () => ({
  state,
  fetchCatalogCategories
});
