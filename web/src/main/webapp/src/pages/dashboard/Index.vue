<template>
  <el-container>
    <el-aside class="p-5">
      <v-card header="Routes" class="mb-8">
        <div class="h-96 overflow-y-scroll">
          <v-route-list @select="fetchRouting"></v-route-list>
        </div>
      </v-card>
      <v-card header="Options">
      </v-card>
    </el-aside>
    <el-main>
      <div class="w-full h-full flex flex-col">
        <options-editor></options-editor>
        <v-transit-map :routing-result="routingResult"></v-transit-map>
      </div>
    </el-main>
  </el-container>
</template>

<script>
import TransitRouter from "./TransitRouter";
import VRouteList from "./RouteList";
import VTransitMap from "./TransitMap";
import OptionsEditor from "./OptionsEditor";

export default {
  name: "Index",
  components: {VTransitMap, VRouteList, OptionsEditor},
  data() {
    return {
      routingResult: null,

    };
  },

  methods: {
    fetchRouting(routeId) {
      TransitRouter.computeRouting(routeId)
          .then(({data}) => {
            this.routingResult = data;
          })
    }
  },

  created() {
  }
}
</script>