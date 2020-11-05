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

        <v-card v-for="evaluation in evaluations" :key="evaluation.name" class="my-8">
          <template #header>
            <div class="flex justify-between items-center p-2">
              <router-link :to="{name: 'evaluation.view', params: {name: evaluation.name}}">
                {{ evaluation.name }}
              </router-link>
              <span>{{ evaluation.createdAt | fromNow }}</span>
            </div>
          </template>
          <div class="flex p-2 gap-8">
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
      </div>
    </v-get>
  </div>
</template>

<script>
import VAccuracyChart from "./AccuracyChart";
import VMetric from "./Metric";
import VEvaluationForm from "./EvaluationForm";
import VEvaluationFormDialog from "./EvaluationFormDialog";

export default {
  name: "Overview",
  components: {VEvaluationFormDialog, VEvaluationForm, VMetric, VAccuracyChart},
  data() {
    return {}
  },
  methods: {}
}
</script>