<template>
  <div class="">
    <div class="grid grid-cols-3 gap-4 px-4">
      <v-card class="">
        <div class="text-center text-secondary py-2">averaged avg Frechet Distance
          <span class="italic">&delta;<sub>aF</sub></span>
        </div>
        <div v-loading="!report.length" class="min-h-128 p-2">
          <HistogramAverageFrechetDistance v-if="report.length"
                                           :report="report"
                                           class="relative h-128"
          ></HistogramAverageFrechetDistance>
        </div>
        <div class="flex w-full justify-between border-t p-2">
          <v-metric :value="info.statistics.fd['max'] | number('0.0')"
                    class="text-red-400"
                    title="max"
                    unit="m"
          ></v-metric>
          <v-metric :value="info.statistics.fd['min'] | number('0.0')"
                    class="text-green-400"
                    title="min"
                    unit="m"
          ></v-metric>
          <v-metric :value="info.statistics.fd['average'] | number('0.0')"
                    class=""
                    title="avg"
                    unit="m"
          ></v-metric>
        </div>
      </v-card>

      <v-card class="">
        <div class="text-center text-secondary py-2">
          percentage mismatched hop segments <span class="italic">A<sub>N</sub></span>
        </div>
        <div v-loading="!report.length" class="min-h-128 p-2">
          <HistogramMismatchedHopSegments v-if="report.length"
                                          :report="report"
                                          class="relative h-128">
          </HistogramMismatchedHopSegments>
        </div>
        <div class="flex w-full justify-between border-t py-2">
          <v-metric :value="info.statistics.an['max'] | number('0.000')"
                    class="text-red-400"
                    title="max"
                    unit=""
          ></v-metric>
          <v-metric :value="info.statistics.an['min'] | number('0.000')"
                    class="text-green-400"
                    title="min"
                    unit=""
          ></v-metric>
          <v-metric :value="info.statistics.an['average'] | number('0.000')"
                    class=""
                    title="avg"
                    unit=""
          ></v-metric>
        </div>

      </v-card>

      <v-card class="">
        <div class="text-center text-secondary py-2">
          percentage length mismatched hop segments <span class="italic">A<sub>L</sub></span>
        </div>
        <div v-loading="!report.length" class="min-h-128 py-2">
          <HistogramLengthMismatchedHopSegments v-if="report.length"
                                                :report="report"
                                                class="relative h-128">
          </HistogramLengthMismatchedHopSegments>
        </div>
        <div class="flex w-full justify-between border-t py-2">
          <v-metric :value="info.statistics.al['max'] | number('0.000')"
                    class="text-red-400"
                    title="max"
                    unit=""
          ></v-metric>
          <v-metric :value="info.statistics.al['min'] | number('0.000')"
                    class="text-green-400"
                    title="min"
                    unit=""
          ></v-metric>
          <v-metric :value="info.statistics.al['average'] | number('0.000')"
                    class=""
                    title="avg"
                    unit=""
          ></v-metric>
        </div>

      </v-card>

      <v-card class="col-span-2">
        <v-report-list :name="$route.params.name"></v-report-list>
      </v-card>

      <v-card class="p-2">
        <ExecutionTimeChart v-if="info" :info="info"></ExecutionTimeChart>

        <div class="my-8">
          <div class="text-center text-secondary mb-4">configuration</div>
          <div class="flex justify-between gap-4">
            <v-metric :value="info.parameters.profile" size="mini" title="Profile"></v-metric>
            <v-metric :value="info.parameters.sigma" size="small" title="sigma"></v-metric>
            <v-metric :value="info.parameters.beta" size="small" title="beta"></v-metric>
            <v-metric :value="info.parameters.candidateSearchRadius" size="small" title="csr"></v-metric>
          </div>

          <div class="flex justify-center gap-4">

            <v-metric :value="info.statistics.trips" size="small" title="Trips"></v-metric>
            <v-metric :value="info.statistics.generatedShapes" size="small" title="Shapes"></v-metric>
          </div>

        </div>


        <div class="flex justify-between w-full my-8">
          <v-metric :value="shapeGenerationErrors.length"
                    class=""
                    title="shape generation errors"
                    unit=""
          ></v-metric>
          <v-metric :value="affectedTripCount"
                    class=""
                    title="affected trips"
                    unit=""
          ></v-metric>
        </div>
      </v-card>


    </div>


    <div class="container">
      <v-card class="my-8">

      </v-card>
    </div>
  </div>
</template>

<script>
import VReportList from "./ReportList";
import HistogramAverageFrechetDistance from "./HistogramAverageFrechetDistance";
import HistogramMismatchedHopSegments from "./HistogramMismatchedHopSegments";
import HistogramLengthMismatchedHopSegments from "./HistogramLengthMismatchedHopSegments";
import VSummary from "./Summary";
import ExecutionTimeChart from "./ExecutionTimeChart";

export default {
  name: "v-finished-view",

  components: {
    ExecutionTimeChart,
    VSummary,
    HistogramLengthMismatchedHopSegments,
    HistogramMismatchedHopSegments, HistogramAverageFrechetDistance, VReportList
  },

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

  computed: {
    shapeGenerationErrors() {
      return this.info.errors.filter(error => error.code === 'shape_generation_failed') ?? [];
    },
    affectedTripCount() {
      return this.shapeGenerationErrors
          .flatMap(error => error.details.trips.length)
          .reduce((sum, count) => sum + count, 0)
    }
  },

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