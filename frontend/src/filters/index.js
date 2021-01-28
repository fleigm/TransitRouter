import fromNow from './fromNow';
import dateFilter from './dateFilter';
import numberFilter from './NumberFilter';
import {routeTypeToString} from "./Filters";

export default {
    install(Vue) {
        Vue.filter('fromNow', fromNow);
        Vue.filter('date', dateFilter);
        Vue.filter('number', numberFilter);
        Vue.filter('routeTypeToString', routeTypeToString);
    }
}