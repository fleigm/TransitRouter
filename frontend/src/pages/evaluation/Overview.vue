<template>
  <div class="container">
    <div class="my-8 flex justify-between">
      <div></div>
      <div class="">
        <v-evaluation-form-dialog></v-evaluation-form-dialog>
      </div>
    </div>
    <v-get resource="eval">
      <div slot-scope="{data : evaluations}">

        <template v-for="evaluation in evaluations">
          <v-evaluation-info-card :evaluation="evaluation"></v-evaluation-info-card>
        </template>
      </div>
    </v-get>
  </div>
</template>

<script>
import VAccuracyChart from "./AccuracyChart";
import VMetric from "./Metric";
import VEvaluationForm from "./EvaluationForm";
import VEvaluationFormDialog from "./EvaluationFormDialog";
import VEvaluationInfoCard from "./EvaluationInfoCard";

export default {
  name: "Overview",
  components: {VEvaluationInfoCard, VEvaluationFormDialog, VEvaluationForm, VMetric, VAccuracyChart},
  data() {
    return {}
  },
  methods: {}
}
</script>