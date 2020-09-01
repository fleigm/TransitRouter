import 'leaflet/dist/leaflet.css'
import {LCircle, LMap, LMarker, LPolyline, LPopup, LTileLayer} from "vue2-leaflet";

export default {
    install(Vue) {
        Vue.component('l-map', LMap)
        Vue.component('l-tile-layer', LTileLayer)
        Vue.component('l-marker', LMarker)
        Vue.component('l-popup', LPopup)
        Vue.component('l-polyline', LPolyline)
        Vue.component('l-circle', LCircle)
    }
}