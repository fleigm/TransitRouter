<template>
  <div class="h-full w-full">
    <div class="">
      <el-checkbox v-model="options.showShape">show shape</el-checkbox>
      <el-checkbox v-model="options.showOriginalShape">show original shape</el-checkbox>
      <el-checkbox v-model="options.showStops">show stops</el-checkbox>
    </div>
    <l-map
        class=""
        :zoom="zoom"
        :center="center"
        :bounds="bounds"
        @update:center="centerUpdated"
        @update:zoom="zoomUpdated"
        @update:bounds="boundsUpdated">
      <l-tile-layer :url="url"/>

      <template v-if="routingResult">
        <routing-result :routing-result="routingResult" :options="options"></routing-result>
      </template>

    </l-map>
  </div>
</template>

<script>
import RoutingResult from "./RoutingResult";
export default {
  name: "v-transit-map",
  components: {RoutingResult},
  props: {
    routingResult: {
      type: Object
    },
  },

  data() {
    return {
      url: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
      zoom: 14,
      center: [47.9959, 7.85222],
      bounds: null,
      options: {
        showShape: true,
        showOriginalShape: true,
        showStops: true,
      },
    }
  },

  methods: {
    zoomUpdated(zoom) {
      this.zoom = zoom;
    },
    centerUpdated(center) {
      this.center = center;
    },
    boundsUpdated(bounds) {
      this.bounds = bounds;
    },
  }
}
</script>