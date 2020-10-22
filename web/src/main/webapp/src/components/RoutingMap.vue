<template>
  <v-map :bounds="bounds">
    <l-animated-polyline :lat-lngs="route.generatedShape"
                         :options="{'delay': 2400, fillOpacity: 0.5, opacity: 0.5}"
                         @ready="setBounds">
    </l-animated-polyline>

    <l-animated-polyline :lat-lngs="route.originalShape"
                         :options="{'delay': 2400, 'color': '#000', fillOpacity: 0.5, opacity: 0.5}"
    ></l-animated-polyline>

    <l-circle v-for="(stop, i) in route.stops"
              :key="i"
              color="red"
              :radius="1"
              :lat-lng="[stop.stop_lat, stop.stop_lon]">
      <l-popup>
        <pre>{{ stop }}</pre>
      </l-popup>
    </l-circle>
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
      console.log("changed");
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