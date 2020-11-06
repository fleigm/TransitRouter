<template>
  <div class="container">
    <v-summary-card :name=name></v-summary-card>
    <v-card class="my-8">
      <v-report-list :name="name"></v-report-list>
    </v-card>

  </div>
</template>

<script>
import VReportList from "./ReportList";
import VSummaryCard from "./Summary";

export default {
  name: "Index",

  components: {VSummaryCard, VReportList},

  computed: {
    name() {
      return this.$route.params.name
    }
  }

}
</script>