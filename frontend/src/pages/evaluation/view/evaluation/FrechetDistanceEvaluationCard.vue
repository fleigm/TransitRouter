<template>
  <EvaluationCard>
    <template #header>
      average Fréchet Distance <span class="italic">&delta;<sub>aF</sub></span>
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
</template>

<script>
import EvaluationCard from "../EvaluationCard";
import HistogramAverageFrechetDistance from "../HistogramAverageFrechetDistance";
import HasEvaluation from "./HasEvaluation";

export default {
  name: "FrechetDistanceEvaluationCard",

  components: {HistogramAverageFrechetDistance, EvaluationCard},

  mixins: [HasEvaluation],

  props: {
    report: {
      type: Array,
      required: true,
    }
  },
}
</script>