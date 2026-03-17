import { createApp } from 'vue';
import ElementPlus from 'element-plus';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import * as ElementPlusIconsVue from '@element-plus/icons-vue';

import App from './App.vue';
import router from './router';
import 'element-plus/dist/index.css';
import './assets/main.css';

const app = createApp(App);

app.use(router);
app.use(ElementPlus, { locale: zhCn });

Object.entries(ElementPlusIconsVue).forEach(([name, component]) => {
  app.component(name, component);
});

app.mount('#app');

