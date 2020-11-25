import 'leaflet/dist/leaflet.css';
import 'leaflet-polylineoffset';
import {LCircle, LCircleMarker, LControl, LMap, LMarker, LPolyline, LPopup, LTileLayer, LTooltip} from "vue2-leaflet";
import AnimatedPolyline from "../components/AnimatedPolyline";

import L from 'leaflet';

delete L.Icon.Default.prototype._getIconUrl;

L.Icon.Default.mergeOptions({
    iconRetinaUrl: require('leaflet/dist/images/marker-icon-2x.png'),
    iconUrl: require('leaflet/dist/images/marker-icon.png'),
    shadowUrl: require('leaflet/dist/images/marker-shadow.png'),
});

export default {
    install(Vue) {
        Vue.component('l-map', LMap)
        Vue.component('l-control', LControl)
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