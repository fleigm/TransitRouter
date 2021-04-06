<template>
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
</template>

<script>
import EvaluationCard from "../EvaluationCard";
import HasEvaluation from "./HasEvaluation";
import HistogramMismatchedHopSegments from "../HistogramMismatchedHopSegments";

export default {
  name: "AnEvaluationCard",

  components: {HistogramMismatchedHopSegments, EvaluationCard},

  mixins: [HasEvaluation],

  props: {
    report: {
      type: Array,
      required: true,
    }
  },
}
</script>