<template>
  <v-map :bounds="bounds">
    <l-animated-polyline :lat-lngs="route.generatedShape"
                         :options="{'delay': 2400, offset: 5}"
                         @ready="setBounds">
    </l-animated-polyline>

    <l-animated-polyline :lat-lngs="route.originalShape"
                         :options="{'delay': 2400, 'color': '#000', offset: 2}"
    ></l-animated-polyline>

    <l-circle-marker v-for="(stop, i) in route.stops"
              :key="i"
              color="red"
              :radius="2"
              :lat-lng="[stop.stop_lat, stop.stop_lon]">
      <l-tooltip>
        <span>Stop # {{ i }}</span>
        <pre>{{ stop }}</pre>
      </l-tooltip>
    </l-circle-marker>
  </v-map>
</template>

<script>
import VMap from "./Map";
import VRoutingResult from "../pages/dashboard/RoutingResult";
import L from 'leaflet';

export default {
  name: "v-routing-map",

  components: {VRoutingResult, VMap},

  props: {
    route: Object
  },

  data() {
    return {
      //bounds: L.latLngBounds(this.route.generatedShape)
    }
  },

  computed: {
    bounds() {
      return L.latLngBounds(this.route.generatedShape)
    }
  },

  methods: {
    setBounds() {

    }
  }
}
</script>

<style scoped>

</style>