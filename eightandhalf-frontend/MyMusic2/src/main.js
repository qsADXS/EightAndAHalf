import { createApp } from 'vue'
import App from './App.vue'
import router from './router/index.ts'
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import {createPinia} from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'


const app = createApp(App)
const pinia=createPinia();
pinia.use(piniaPluginPersistedstate)
for(const[key,component] of Object.entries(ElementPlusIconsVue)){
    app.component(key,component);
}
app.use(router) //注册路由
    .use(ElementPlus) //注册element-plus
    .use(pinia)
    .mount('#app')

export default pinia