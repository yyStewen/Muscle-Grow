import { computed, reactive } from 'vue';

const STORAGE_KEY = 'mg-user-profile';

const loadProfile = () => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch (error) {
    return null;
  }
};

const state = reactive({
  profile: loadProfile()
});

const saveProfile = (profile) => {
  state.profile = profile;
  localStorage.setItem(STORAGE_KEY, JSON.stringify(profile));
};

const clearProfile = () => {
  state.profile = null;
  localStorage.removeItem(STORAGE_KEY);
};

const isLoggedIn = computed(() => Boolean(state.profile?.name));

export const useAuthStore = () => {
  const login = (payload) => {
    saveProfile({
      name: payload.name,
      phone: payload.phone || '',
      avatar: payload.avatar || ''
    });
  };

  const logout = () => {
    clearProfile();
  };

  return {
    state,
    isLoggedIn,
    login,
    logout
  };
};

