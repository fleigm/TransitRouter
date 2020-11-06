import 'leaflet/dist/leaflet.css';
import 'leaflet-polylineoffset';
import {LCircle, LMap, LMarker, LPolyline, LPopup, LTileLayer, LCircleMarker, LTooltip} from "vue2-leaflet";
import AnimatedPolyline from "../components/AnimatedPolyline";

export default {
    install(Vue) {
        Vue.component('l-map', LMap)
        Vue.component('l-tile-layer', LTileLayer)
        Vue.component('l-marker', LMarker)
        Vue.component('l-circle-marker', LCircleMarker)
        Vue.component('l-tooltip', LTooltip)
        Vue.component('l-popup', LPopup)
        Vue.component('l-circle', LCircle)
        Vue.component('l-polyline', LPolyline)
        Vue.component(AnimatedPolyline.name, AnimatedPolyline)
    }
}