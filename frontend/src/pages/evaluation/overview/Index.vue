<template>
  <div class="container">
    <div class="my-8 flex justify-between">
      <div></div>
      <div class="">
        <el-button size="mini" @click="clearCache">Clear Cache</el-button>
        <v-evaluation-form-dialog></v-evaluation-form-dialog>
      </div>
    </div>

    <v-evaluation-info-card v-for="evaluation in evaluations"
                            :key="evaluation.name"
                            :evaluation="evaluation"
    ></v-evaluation-info-card>
    <div v-if="!evaluations.length">
      <div class="text-secondary text-2xl font-thin text-center py-8">There are currently no evaluations.</div>
    </div>
  </div>
</template>

<script>
import VAccuracyChart from "../AccuracyChart";
import VMetric from "../Metric";
import VEvaluationForm from "./EvaluationForm";
import VEvaluationFormDialog from "./EvaluationFormDialog";
import VEvaluationInfoCard from "./EvaluationInfoCard";
import EvaluationService from "../EvaluationService";

export default {
  name: "Overview",

  components: {VEvaluationInfoCard, VEvaluationFormDialog, VEvaluationForm, VMetric, VAccuracyChart},

  computed: {
    evaluations() {
      return EvaluationService.state.evaluations;
    }
  },

  methods: {
    clearCache() {
      EvaluationService.clearCache()
    }
  },

  mounted() {
    EvaluationService.fetchEvaluations();
  }
}
</script>