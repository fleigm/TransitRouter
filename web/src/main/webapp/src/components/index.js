import Promise from './Promise'
import Spinner from "./Spinner";
import Card from "./Card";
import Resource from "./Resource";
import RoutingMap from "./RoutingMap";

export default {
    install(Vue) {
        Vue.component(Promise.name, Promise);
        Vue.component(Spinner.name, Spinner);
        Vue.component(Card.name, Card);
        Vue.component(Resource.name, Resource);
        Vue.component(RoutingMap.name, RoutingMap);
    }
}