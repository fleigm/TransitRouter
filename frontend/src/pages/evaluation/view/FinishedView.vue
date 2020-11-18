<template>
  <div>
    <v-summary-card :info=info></v-summary-card>

    <v-card class="my-8">
      <v-report-list :name="$route.params.name"></v-report-list>
    </v-card>
  </div>
</template>

<script>
import VSummaryCard from "./SummaryCard";
import VReportList from "./ReportList";

export default {
  name: "v-finished-view",

  components: {VReportList, VSummaryCard},

  props: {
    info: {
      type: Object,
      required: true,
    }
  }
}
</script>

<style scoped>

</style>