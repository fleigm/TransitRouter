<template>
  <v-card class="my-8" header="Summary">
    <v-promise :promise="fetch">
      <template #default="{data : summary}">
        <div class="flex p-2">
          <div>
            <div class="flex justify-between">
              <div class="flex flex-col items-center justify-center p-2 text-red-400">
                <div><span class="text-4xl font-thin">{{ summary.highestAvgFd.avgFd }}</span><span>m</span></div>
                <div>Highest avgFd</div>
                <div class="text-secondary">{{ summary.highestAvgFd.tripId }}</div>
              </div>
              <div class="flex flex-col items-center justify-center p-2 text-green-400">
                <div><span class="text-4xl font-thin">{{ summary.lowestAvgFd.avgFd }}</span><span>m</span></div>
                <div>Lowest avgFd</div>
                <div class="text-secondary">{{ summary.lowestAvgFd.tripId }}</div>
              </div>
            </div>
            <div>
              <v-accuracy-chart :accuracies="summary.accuracies" :height="200" :width="300"></v-accuracy-chart>
            </div>
          </div>

          <div class="w-full">
            <v-map :center="summary.highestAvgFd.details.originalShape[0]">
              <v-routing-result :routing-result="summary.highestAvgFd.details"></v-routing-result>
              <v-routing-result :routing-result="summary.lowestAvgFd.details"></v-routing-result>
            </v-map>
          </div>
        </div>
      </template>
      <template #pending>
        <div v-loading="true" class="h-64 w-full"></div>
      </template>
    </v-promise>
  </v-card>
</template>

<script>
import VAccuracyChart from "./AccuracyChart";
import VRoutingResult from "../dashboard/RoutingResult";
import VMap from "../../components/Map";
export default {
  name: "v-summary-card",
  components: {VMap, VRoutingResult, VAccuracyChart},
  data() {
    return {
      fetch: this.$http.get('eval/stuttgart'),
    }
  }
}
</script>
