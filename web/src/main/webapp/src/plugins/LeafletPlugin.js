import 'leaflet/dist/leaflet.css';
import 'leaflet-polylineoffset';
import {LCircle, LMap, LMarker, LPolyline, LPopup, LTileLayer} from "vue2-leaflet";
import AnimatedPolyline from "../components/AnimatedPolyline";

export default {
    install(Vue) {
        Vue.component('l-map', LMap)
        Vue.component('l-tile-layer', LTileLayer)
        Vue.component('l-marker', LMarker)
        Vue.component('l-popup', LPopup)
        Vue.component('l-polyline', LPolyline)
        Vue.component('l-circle', LCircle)
        Vue.component(AnimatedPolyline.name, AnimatedPolyline)
    }
}