<template>
  <el-container>
    <div class="h-full w-full">
      <l-map
          class=""
          :zoom="zoom"
          :center="center"
          :bounds="bounds"
          @click="findCandidates"
          @update:center="centerUpdated"
          @update:zoom="zoomUpdated"
          @update:bounds="boundsUpdated">
        <l-tile-layer :url="url"/>

        <l-circle v-for="(candidate, i) in candidates"
                  :key="i"
                  :radius="1"
                  :lat-lng="candidate.point"
        ></l-circle>

        <template v-for="(candidate, i) in candidates">
          <l-animated-polyline v-for="(direction) in candidate.directions"
                      :lat-lngs="direction"
                      :options="{offset: i, opacity: 0.5, delay: 5000}"
          ></l-animated-polyline>
        </template>

      </l-map>
    </div>
  </el-container>
</template>

<script>
export default {
  name: "v-candidate-finder",

  data() {
    return {
      url: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
      zoom: 14,
      center: [47.9959, 7.85222],
      bounds: null,

      candidates: [],
    }
  },

  methods: {
    findCandidates(event) {
      console.log(event);
      this.$http(`candidates`, {params: {lat: event.latlng.lat, lon: event.latlng.lng, radius: 25, profile: 'rail'}})
          .then(({data}) => {
            this.candidates = data;
          })
    },
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
