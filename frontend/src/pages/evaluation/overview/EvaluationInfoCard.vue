<template>
  <v-card class="my-8">
    <template #header>
      <div class="flex justify-between items-center p-2">
        <div class="flex gap-4">
          <router-link :to="{name: 'evaluation.view', params: {name: evaluation.name}}">
            {{ evaluation.name }}
          </router-link>
          <div v-if="!isFinished" class="text-secondary rounded-full border border-blue-400 px-2">
            {{ evaluation.status }}
          </div>
        </div>
        <span>{{ evaluation.createdAt | fromNow }}</span>
      </div>
    </template>
    <div v-if="!isFinished" class="p-2">
      <div class="flex gap-4 mb-2">
        <v-metric :value="evaluation.parameters.profile" size="mini" title="Profile"></v-metric>
        <v-metric :value="evaluation.parameters.alpha" size="small" title="alpha"></v-metric>
        <v-metric :value="evaluation.parameters.beta" size="small" title="beta"></v-metric>
        <v-metric :value="evaluation.parameters.candidateSearchRadius" size="small" title="csr"></v-metric>
        <v-metric :value="evaluation.parameters.uTurnDistancePenalty" size="small"
                  title="uturn penalty"></v-metric>
      </div>
    </div>
    <v-summary v-if="isFinished" :info="evaluation" class="flex p-2 gap-8">
    </v-summary>
  </v-card>
</template>

<script>
import VMetric from "../Metric";
import VAccuracyChart from "../AccuracyChart";
import VSummary from "../view/Summary";

export default {
  name: "v-evaluation-info-card",

  components: {VSummary, VAccuracyChart, VMetric},

  props: {
    evaluation: {
      type: Object,
      required: true,
    }
  },

  computed: {
    isPending() {
      return this.evaluation.status === 'PENDING';
    },
    isFinished() {
      return this.evaluation.status === 'FINISHED';
    }
  }

}
</script>