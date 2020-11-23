<template>
  <v-map :bounds="bounds">
    <l-polyline :lat-lngs="route.generatedShape"
                         :options="{'delay': 2400, offset: 5}"
                         @ready="setBounds">
    </l-polyline>

    <l-polyline :lat-lngs="route.originalShape"
                         :options="{'delay': 2400, 'color': '#000', offset: 2}"
    ></l-polyline>

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

    <l-control position="bottomleft">
      <div class="flex gap-5 px-2 bg-white">
        <span class="text-blue-600">original shape</span>
        <span class="text-black">generated shape</span>
        <span class="text-red-600">stop</span>
      </div>
    </l-control>
  </v-map>
</template>

<script>
import VMap from "./Map";
import L from 'leaflet';

export default {
  name: "v-routing-map",

  components: {VMap},

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