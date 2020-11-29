<template>
  <div>
    <v-summary-card :info=info></v-summary-card>

    <div class="flex gap-4">
      <v-card v-loading="!report.length" class="w-1/2 my-8 p-2 min-h-128">
        <div class="text-center text-secondary mb-4">averaged avg Frechet Distance <span
            class="italic">&delta;<sub>aF</sub></span></div>
        <HistogramAverageFrechetDistance v-if="report.length"
                                         :report="report"
                                         class="relative h-128"
        ></HistogramAverageFrechetDistance>
      </v-card>
      <v-card v-loading="!report.length" class="w-1/2 my-8 p-2 min-h-128">
        <div class="text-center text-secondary mb-4">percentage mismatched hop segments <span
            class="italic">A<sub>N</sub></span></div>
        <HistogramMismatchedHopSegments v-if="report.length"
                                        :report="report"
                                        class="relative h-128">
        </HistogramMismatchedHopSegments>
      </v-card>
    </div>


    <v-card class="my-8">
      <v-report-list :name="$route.params.name"></v-report-list>
    </v-card>
  </div>
</template>

<script>
import VSummaryCard from "./SummaryCard";
import VReportList from "./ReportList";
import HistogramAverageFrechetDistance from "./HistogramAverageFrechetDistance";
import HistogramMismatchedHopSegments from "./HistogramMismatchedHopSegments";

export default {
  name: "v-finished-view",

  components: {HistogramMismatchedHopSegments, HistogramAverageFrechetDistance, VReportList, VSummaryCard},

  props: {
    info: {
      type: Object,
      required: true,
    }
  },

  data() {
    return {
      report: [],
    }
  },

  computed: {},

  methods: {
    fetchReport() {
      const name = this.$route.params.name;
      this.$http.get(`eval/${name}/report?page=1&limit=${999999}`)
          .then(({data: page}) => {
            this.report = page.data;
          })
    }
  },

  mounted() {
    this.fetchReport();
  }
}
</script>