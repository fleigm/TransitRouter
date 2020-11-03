<template>
  <div class="container">
    <v-get resource="eval">
      <div slot-scope="{data : evaluations}">

        <v-card v-for="evaluation in evaluations" :key="evaluation.name" class="my-8">
          <template #header>
            <div class="flex justify-between items-center p-2">
              <router-link :to="{name: 'evaluation.view', params: {name: evaluation.name}}">
                {{ evaluation.name }}
              </router-link>
              <span>{{ evaluation.datetime }}</span>
            </div>
          </template>
          <div class="flex p-2 gap-8">
            <div>
              <div class="flex justify-center gap-4">
                <v-metric :value="evaluation.statistics['highestAvgFd.value']" class="text-red-400"
                          title="Highest avgFd"
                          unit="m"></v-metric>
                <v-metric :value="evaluation.statistics['lowestAvgFd.value']" class="text-green-400"
                          title="Lowest avgFd"
                          unit="m"></v-metric>
                <v-metric :value="evaluation.statistics['averagedAvgFd']" class="" title="Average avgFd"
                          unit="m"></v-metric>
              </div>
              <v-accuracy-chart :accuracies="evaluation.statistics['accuracy']" :height="200"
                                :width="300"></v-accuracy-chart>
            </div>
            <div>
              <div class="flex justify-center gap-4">
                <v-metric :value="evaluation.statistics.trips" title="Trips"></v-metric>
                <v-metric :value="evaluation.statistics.generatedShapes" title="Shapes"></v-metric>
              </div>

              <!--<div class="flex justify-center gap-4">
                <v-metric title="Total" :value="evaluation.statistics.execution_times.total" unit="s"></v-metric>
                <v-metric title="Shape generation" :value="evaluation.statistics.execution_times.shape_generation"
                          unit="s"></v-metric>
                <v-metric title="Evaluation" :value="evaluation.statistics.execution_times.evaluation" unit="s"></v-metric>
              </div>-->
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

export default {
  name: "Overview",
  components: {VMetric, VAccuracyChart},
  data() {
    return {}
  },
  methods: {}
}
</script>