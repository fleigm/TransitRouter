<template>
  <l-map
      class=""
      :zoom="zoom"
      :center="center"
      :bounds="bounds"
      @update:center="centerUpdated"
      @update:zoom="zoomUpdated"
      @update:bounds="boundsUpdated">
    <l-tile-layer :url="url"/>
    <slot></slot>
  </l-map>
</template>

<script>
export default {
  name: "v-transit-map",

  data() {
    return {
      url: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
      zoom: 14,
      center: [47.9959, 7.85222],
      bounds: null,
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