<template>
  <el-container>
    <el-aside class="p-5">
      <v-card header="Routes" class="mb-8">
        <div class="h-96 overflow-y-scroll">
          <v-route-list @select="fetchRouting"></v-route-list>
        </div>
      </v-card>
      <v-card header="Trips">
        <div class="h-96 overflow-y-scroll">

        </div>
      </v-card>
    </el-aside>
    <el-main>
      <div class="w-full h-full">
        <v-transit-map>
          <l-polyline :lat-lngs="route"></l-polyline>

          <template v-for="timeStep in timeSteps">
            <l-circle v-for="candidate in timeStep.candidates"
                      :key="candidate.id"
                      :radius="1"
                      :lat-lng="candidate.state.position">
              <l-popup>
                <pre>{{ candidate }}</pre>
              </l-popup>
            </l-circle>
          </template>


          <!--<template v-for="candidates in candidates">
            <l-circle v-for="candidate in candidates"
                      :radius="1"
                      :lat-lng="candidate">
            </l-circle>
          </template>
          <l-circle v-for="stop in stops"
                    :key="stop.id"
                    color="red"
                    :radius="2"
                    :lat-lng="[stop.stop_lat, stop.stop_lon]">
            <l-popup>
              <pre>{{ stop }}</pre>
            </l-popup>
          </l-circle>-->
        </v-transit-map>
      </div>
    </el-main>
  </el-container>
</template>

<script>
import axios from 'axios'
import VRouteList from "./RouteList";
import VTransitMap from "./TransitMap";

export default {
  name: "Index",
  components: {VTransitMap, VRouteList},
  data() {
    return {
      stops: [],
      route: [],
      candidates: [],
      timeSteps: []
    };
  },

  methods: {
    fetchRouting(routeId) {
      this.$http.get(`routing/${routeId}`)
          .then(({data}) => {
            this.stops = data.stops;
            this.route = data.route;
            this.candidates = data.candidates;
            this.timeSteps = data.timeSteps;
          })
    }
  },

  created() {
  }
}
</script>