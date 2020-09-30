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
        <v-transit-map :routing-result="routingResult"></v-transit-map>
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
      routingResult: null,

    };
  },

  methods: {
    fetchRouting(routeId) {
      this.$http.get(`routing/${routeId}`)
          .then(({data}) => {
            this.routingResult = data;
          })
    }
  },

  created() {
  }
}
</script>