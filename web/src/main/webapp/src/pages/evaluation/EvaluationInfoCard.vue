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
    <div v-if="isFinished" class="flex p-2 gap-8">
      <div class="w-1/2">
        <div class="flex justify-center gap-4">
          <v-metric :value="evaluation.statistics['highestAvgFd.value'] | number('0.00')"
                    class="text-red-400"
                    title="Highest avgFd"
                    unit="m"
          ></v-metric>
          <v-metric :value="evaluation.statistics['lowestAvgFd.value'] | number('0.00')"
                    class="text-green-400"
                    title="Lowest avgFd"
                    unit="m"
          ></v-metric>
          <v-metric :value="evaluation.statistics['averagedAvgFd'] | number('0.00')"
                    class=""
                    title="Average avgFd"
                    unit="m"
          ></v-metric>
        </div>
        <v-accuracy-chart :accuracies="evaluation.statistics['accuracy']"
                          :height="150"
                          :width="300"></v-accuracy-chart>
      </div>
      <div class="w-1/2">
        <div class="flex justify-center gap-4 mb-2">
          <v-metric :value="evaluation.parameters.alpha" size="small" title="alpha"></v-metric>
          <v-metric :value="evaluation.parameters.beta" size="small" title="beta"></v-metric>
          <v-metric :value="evaluation.parameters.candidateSearchRadius" size="small" title="csr"></v-metric>
          <v-metric :value="evaluation.parameters.uTurnDistancePenalty" size="small"
                    title="uturn penalty"></v-metric>
        </div>

        <div class="flex justify-center gap-4 mb-2">
          <v-metric :value="evaluation.parameters.profile" size="mini" title="Profile"></v-metric>
        </div>

        <div class="flex justify-center gap-4 mb-2">
          <v-metric :value="evaluation.statistics.trips" size="small" title="Trips"></v-metric>
          <v-metric :value="evaluation.statistics.generatedShapes" size="small" title="Shapes"></v-metric>
        </div>

        <div class="flex justify-center gap-4 mb-2">
          <v-metric :value="123"
                    size="small"
                    title="Total"
                    unit="s"
          ></v-metric>
          <v-metric :value="123"
                    size="small"
                    title="Shape generation"
                    unit="s"
          ></v-metric>
          <v-metric :value="123"
                    size="small"
                    title="Evaluation"
                    unit="s"
          ></v-metric>
        </div>
      </div>
    </div>
  </v-card>
</template>

<script>
import VMetric from "./Metric";
import VAccuracyChart from "./AccuracyChart";

export default {
  name: "v-evaluation-info-card",

  components: {VAccuracyChart, VMetric},

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