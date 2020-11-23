import 'leaflet/dist/leaflet.css';
import 'leaflet-polylineoffset';
import {
    LCircle,
    LMap,
    LMarker,
    LPolyline,
    LPopup,
    LTileLayer,
    LCircleMarker,
    LTooltip,
    LControl
} from "@vue-leaflet/vue-leaflet";

export default {
    install(app) {
        app.component('l-map', LMap)
        app.component('l-control', LControl)
        app.component('l-tile-layer', LTileLayer)
        app.component('l-marker', LMarker)
        app.component('l-circle-marker', LCircleMarker)
        app.component('l-tooltip', LTooltip)
        app.component('l-popup', LPopup)
        app.component('l-circle', LCircle)
        app.component('l-polyline', LPolyline)
    }
}