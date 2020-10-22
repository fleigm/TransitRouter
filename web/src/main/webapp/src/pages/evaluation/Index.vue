<template>
  <div class="container">
    <v-summary-card></v-summary-card>
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
import VSummaryCard from "./Summary";

export default {
  name: "Index",
  components: {VSummaryCard, VRoutingResult, VMap, VAccuracyChart, VReportList},

  data() {
    return {
      details: null
    }
  },

  methods: {
    fetchTrip() {
      this.$http
          .get('eval/stuttgart/trips/6.T3.34-75-j20-2.1.H')
          .then(({data}) => {
            this.details = data;
          })
    },
  },

  created() {
    this.fetchTrip();
  }
}
</script>

<style scoped>

</style>