import Promise from './Promise'
import Spinner from "./Spinner";
import Card from "./Card";
import Resource from "./Resource";
import RoutingMap from "./RoutingMap";
import Get from "./Get";

export default {
    install(app) {
        app.component(Promise.name, Promise);
        app.component(Get.name, Get);
        app.component(Spinner.name, Spinner);
        app.component(Card.name, Card);
        app.component(Resource.name, Resource);
        app.component(RoutingMap.name, RoutingMap);
    }
}