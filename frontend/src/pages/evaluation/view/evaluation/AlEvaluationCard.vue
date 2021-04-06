<template>
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
</template>

<script>
import EvaluationCard from "../EvaluationCard";
import HasEvaluation from "./HasEvaluation";
import HistogramLengthMismatchedHopSegments from "../HistogramLengthMismatchedHopSegments";

export default {
  name: "AlEvaluationCard",

  components: {HistogramLengthMismatchedHopSegments, EvaluationCard},

  mixins: [HasEvaluation],

  props: {
    report: {
      type: Array,
      required: true,
    }
  },
}
</script>