import fromNow from './fromNow';
import dateFilter from './dateFilter';

export default {
    install(Vue) {
        Vue.filter('from-now', fromNow);
        Vue.filter('date', dateFilter);
    }
}