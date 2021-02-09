<template>
  <EvaluationCard header="accuracy">
    <v-accuracy-chart :accuracies="evaluation.quickStats.accuracy" class="min-h-128"></v-accuracy-chart>
  </EvaluationCard>
</template>

<script>
import HasEvaluation from "./HasEvaluation";
import EvaluationCard from "../EvaluationCard";
import VAccuracyChart from "../../AccuracyChart";

export default {
  name: "AccuracyEvaluationCard",
  components: {VAccuracyChart, EvaluationCard},
  mixins: [HasEvaluation]
}
</script>

<style scoped>

</style>