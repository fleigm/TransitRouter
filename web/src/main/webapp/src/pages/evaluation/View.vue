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

}
</script>

<style scoped>

</style>