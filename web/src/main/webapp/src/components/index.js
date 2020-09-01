import Promise from './Promise'
import Spinner from "./Spinner";
import Card from "./Card";

export default {
    install(Vue) {
        Vue.component(Promise.name, Promise);
        Vue.component(Spinner.name, Spinner);
        Vue.component(Card.name, Card);
    }
}