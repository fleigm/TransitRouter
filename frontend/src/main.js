import './index.css';
import './installVueCompositionApi';
import Vue from 'vue';
import VueRouter from "vue-router";
import router from './router';
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/reset.css';
import 'element-ui/lib/theme-chalk/index.css';
import locale from 'element-ui/lib/locale/lang/en';
import VuePortal from '@linusborg/vue-simple-portal';
import './ChartjsSetup';
import LeafletPlugin from "./plugins/LeafletPlugin";
import HttpPlugin from "./plugins/HttpPlugin";
import EventPlugin from "./plugins/EventPlugin";
import Components from './components';
import Filters from './filters';
import App from './App';
import BreadcrumbsPlugin from "./breadcrumbs/Plugin";

Vue.use(VueRouter);
Vue.use(ElementUI, {locale});
Vue.use(VuePortal);
Vue.use(HttpPlugin);
Vue.use(EventPlugin);
Vue.use(LeafletPlugin);
Vue.use(Components);
Vue.use(Filters);
Vue.use(BreadcrumbsPlugin)


const app = new Vue({
    router,
    render: h => h(App),
});

app.$mount('app');

window.app = app;