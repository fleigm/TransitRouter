import Breadcrumbs from "./index";
import BreadcrumbsComponent from './Breadcrumbs';

export default {
    install(Vue, options = {}) {
        Vue.component(BreadcrumbsComponent.name, BreadcrumbsComponent);
        Vue.prototype.$breadcrumbs = Breadcrumbs;
    }
}