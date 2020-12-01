import Promise from './Promise'
import Spinner from "./Spinner";
import Card from "./Card";
import Resource from "./Resource";
import RoutingMap from "./RoutingMap";
import Get from "./Get";
import Histogram from "./Histogram";
import Metric from "./Metric";
import DoughnutChart from "./DoughnutChart";
import HelperTooltip from "./HelperTooltip";

export default {
    install: function (Vue) {
        Vue.component(Promise.name, Promise);
        Vue.component(Get.name, Get);
        Vue.component(Spinner.name, Spinner);
        Vue.component(Card.name, Card);
        Vue.component(Resource.name, Resource);
        Vue.component(RoutingMap.name, RoutingMap);
        Vue.component(Histogram.name, Histogram);
        Vue.component(Metric.name, Metric);
        Vue.component(DoughnutChart.name, DoughnutChart);
        Vue.component(HelperTooltip.name, HelperTooltip);
    }
}