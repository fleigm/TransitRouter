<template>
  <div class="h-full w-full">
    <div>
      <el-checkbox v-model="showShape">show shape</el-checkbox>
      <el-checkbox v-model="showOriginalShape">show original shape</el-checkbox>
      <el-checkbox v-model="showStops">show stops</el-checkbox>
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
        <l-animated-polyline v-if="showShape"
                             :lat-lngs="routingResult.shape"
                             :options="{'delay': 2400}"
        ></l-animated-polyline>

        <l-animated-polyline v-if="showOriginalShape"
                             :lat-lngs="routingResult.originalShape"
                             :options="{'delay': 2400, 'color': '#000'}"
        ></l-animated-polyline>

        <template v-for="timeStep in routingResult.timeSteps">
          <l-circle v-for="candidate in timeStep.candidates"
                    :key="candidate.id"
                    :radius="1"
                    :lat-lng="candidate.state.position">
            <l-popup>
              <pre>{{ candidate }}</pre>
            </l-popup>
          </l-circle>
        </template>

        <l-circle v-if="showStops"
                  v-for="stop in routingResult.stops"
                  :key="stop.id"
                  color="red"
                  :radius="2"
                  :lat-lng="[stop.stop_lat, stop.stop_lon]">
          <l-popup>
            <pre>{{ stop }}</pre>
          </l-popup>
        </l-circle>
      </template>

    </l-map>
  </div>
</template>

<script>
export default {
  name: "v-transit-map",

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
      showShape: true,
      showOriginalShape: true,
      showStops: true,
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