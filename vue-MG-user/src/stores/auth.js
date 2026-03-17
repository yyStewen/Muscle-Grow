import { computed, reactive } from 'vue';

const STORAGE_KEY = 'mg-user-auth';

const loadAuthState = () => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return {
        token: '',
        profile: null
      };
    }

    const parsed = JSON.parse(raw);
    return {
      token: parsed?.token || '',
      profile: parsed?.profile || null
    };
  } catch (error) {
    return {
      token: '',
      profile: null
    };
  }
};

const state = reactive(loadAuthState());

const persistAuthState = () => {
  localStorage.setItem(
    STORAGE_KEY,
    JSON.stringify({
      token: state.token,
      profile: state.profile
    })
  );
};

const saveAuthState = (payload) => {
  state.token = payload?.token || '';
  state.profile = payload?.profile || null;
  persistAuthState();
};

export const clearAuthState = () => {
  state.token = '';
  state.profile = null;
  localStorage.removeItem(STORAGE_KEY);
};

export const getToken = () => state.token || '';

const isLoggedIn = computed(() => Boolean(state.token));

export const useAuthStore = () => {
  const login = (payload) => {
    saveAuthState({
      token: payload.token,
      profile: {
        id: payload.id,
        name: payload.name,
        phone: payload.phone,
        avatar: payload.avatar || ''
      }
    });
  };

  const logout = () => {
    clearAuthState();
  };

  return {
    state,
    isLoggedIn,
    login,
    logout
  };
};
