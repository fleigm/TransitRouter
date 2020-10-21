<template>
  <div class="container">
    <v-card class="my-8" header="Summary" v-if="summary">
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

    </v-card>
    <v-card class="my-8">
      <v-report-list name="stuttgart"></v-report-list>
    </v-card>
  </div>
</template>

<script>
import VReportList from "./ReportList";
import VAccuracyChart from "./AccuracyChart";
import VMap from "../../components/Map";
import VRoutingResult from "../dashboard/RoutingResult";

export default {
  name: "Index",
  components: {VRoutingResult, VMap, VAccuracyChart, VReportList},

  data() {
    return {
      summary: null
    }
  },

  methods: {
    fetchSummary() {
      this.$http
          .get('eval/stuttgart')
          .then(({data}) => {
            this.summary = data;
          })
    },
  },

  created() {
    this.fetchSummary();
  }
}
</script>

<style scoped>

</style>