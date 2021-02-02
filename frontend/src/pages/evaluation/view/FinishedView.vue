<template>
  <div class="pb-8">
    <div class="grid grid-cols-3 gap-4 px-4">
      <EvaluationCard header="accuracy">
        <v-accuracy-chart :accuracies="evaluation.quickStats.accuracy" class="h-48"></v-accuracy-chart>
      </EvaluationCard>

      <EvaluationCard header="configuration">
        <div class="flex flex-col justify-between gap-4">
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
      </EvaluationCard>

      <EvaluationCard header="execution times">
        <ExecutionTimeChart v-if="info" :info="info"></ExecutionTimeChart>
      </EvaluationCard>

      <EvaluationCard>
        <template #header>
          averaged avg Frechet Distance <span class="italic">&delta;<sub>aF</sub></span>
        </template>
        <div v-loading="!report.length" class="min-h-128">
          <HistogramAverageFrechetDistance v-if="report.length"
                                           :report="report"
                                           class="relative h-128"
          ></HistogramAverageFrechetDistance>
        </div>
        <div slot="footer" class="flex w-full justify-between">
          <v-metric :value="evaluation.quickStats.fd.max | number('0.0')"
                    class="text-red-400"
                    title="max"
                    unit="m"
          ></v-metric>
          <v-metric :value="evaluation.quickStats.fd.min | number('0.0')"
                    class="text-green-400"
                    title="min"
                    unit="m"
          ></v-metric>
          <v-metric :value="evaluation.quickStats.fd.average | number('0.0')"
                    class=""
                    title="avg"
                    unit="m"
          ></v-metric>
        </div>
      </EvaluationCard>

      <EvaluationCard>
        <div slot="header">
          percentage mismatched hop segments <span class="italic">A<sub>N</sub></span>
        </div>
        <div v-loading="!report.length" class="min-h-128">
          <HistogramMismatchedHopSegments v-if="report.length"
                                          :report="report"
                                          class="relative h-128">
          </HistogramMismatchedHopSegments>
        </div>
        <div slot="footer" class="flex w-full justify-between">
          <v-metric :value="evaluation.quickStats.an.max | number('0.000')"
                    class="text-red-400"
                    title="max"
                    unit=""
          ></v-metric>
          <v-metric :value="evaluation.quickStats.an.min | number('0.000')"
                    class="text-green-400"
                    title="min"
                    unit=""
          ></v-metric>
          <v-metric :value="evaluation.quickStats.an.average | number('0.000')"
                    class=""
                    title="avg"
                    unit=""
          ></v-metric>
        </div>

      </EvaluationCard>

      <EvaluationCard>
        <div slot="header">
          percentage length mismatched hop segments <span class="italic">A<sub>L</sub></span>
        </div>
        <div v-loading="!report.length" class="min-h-128">
          <HistogramLengthMismatchedHopSegments v-if="report.length"
                                                :report="report"
                                                class="relative h-128">
          </HistogramLengthMismatchedHopSegments>
        </div>
        <div slot="footer" class="flex w-full justify-between">
          <v-metric :value="evaluation.quickStats.al.max | number('0.000')"
                    class="text-red-400"
                    title="max"
                    unit=""
          ></v-metric>
          <v-metric :value="evaluation.quickStats.al.min | number('0.000')"
                    class="text-green-400"
                    title="min"
                    unit=""
          ></v-metric>
          <v-metric :value="evaluation.quickStats.al.average | number('0.000')"
                    class=""
                    title="avg"
                    unit=""
          ></v-metric>
        </div>
      </EvaluationCard>


      <div class="grid auto-rows-min gap-4">
        <EvaluationCard header="shape generation errors">
          <div class="flex justify-between w-full">
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
        </EvaluationCard>
      </div>

      <v-card class="col-span-2">
        <v-report-list :name="$route.params.name"></v-report-list>
      </v-card>
    </div>

  </div>
</template>

<script>
import VReportList from "./ReportList";
import HistogramAverageFrechetDistance from "./HistogramAverageFrechetDistance";
import HistogramMismatchedHopSegments from "./HistogramMismatchedHopSegments";
import HistogramLengthMismatchedHopSegments from "./HistogramLengthMismatchedHopSegments";
import ExecutionTimeChart from "./ExecutionTimeChart";
import EvaluationCard from "./EvaluationCard";
import VAccuracyChart from "../AccuracyChart";
import ErrorCard from "./ErrorCard";

export default {
  name: "v-finished-view",

  components: {
    ErrorCard,
    VAccuracyChart,
    EvaluationCard,
    ExecutionTimeChart,
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
    evaluation() {
      return this.info.extensions['de.fleigm.ptmm.eval.EvaluationExtension'];
    },

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
          .then(({data}) => {
            this.report = data.entries;
          })
    }
  },

  mounted() {
    this.fetchReport();
  }
}
</script>