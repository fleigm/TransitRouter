<template>
  <div class="container">
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
            <v-metric title="Highest avgFd" :value="evaluation.statistics.highestAvgFd.value" unit="m"
                      class="text-red-400"></v-metric>
            <v-metric title="Lowest avgFd" :value="evaluation.statistics.lowestAvgFd.value" unit="m"
                      class="text-green-400"></v-metric>
            <v-metric title="Average avgFd" :value="evaluation.statistics.averaged_avg_fd" unit="m" class=""></v-metric>
          </div>
          <v-accuracy-chart :accuracies="evaluation.statistics.accuracies" :height="200" :width="300"></v-accuracy-chart>
        </div>

        <div>
          <div class="flex justify-center gap-4">
            <v-metric title="Trips" :value="evaluation.statistics.trips"></v-metric>
            <v-metric title="Shapes" :value="evaluation.statistics.generated_shapes"></v-metric>
          </div>

          <div class="flex justify-center gap-4">
            <v-metric title="Total" :value="evaluation.statistics.execution_times.total" unit="s"></v-metric>
            <v-metric title="Shape generation" :value="evaluation.statistics.execution_times.shape_generation"
                      unit="s"></v-metric>
            <v-metric title="Evaluation" :value="evaluation.statistics.execution_times.evaluation" unit="s"></v-metric>
          </div>
        </div>
      </div>


    </v-card>
  </div>
</template>

<script>
import VAccuracyChart from "./AccuracyChart";
import VMetric from "./Metric";

export default {
  name: "Overview",
  components: {VMetric, VAccuracyChart},
  data() {
    return {
      evaluations: [
        {
          "name": "stuttgart",
          "datetime": "2007-12-03T10:15:30.000Z",
          "statistics": {
            "trips": 53021,
            "generated_shapes": 12432,
            "execution_times": {
              "total": "2000",
              "shape_generation": "600",
              "writing_gtfs_feed": "300",
              "evaluation": "1100"
            },
            "accuracies": [
              0.2,
              0.5,
              0.6,
              0.8
            ],
            "highestAvgFd": {
              "tripId": "6.T3.34-75-j20-2.1.H",
              "value": 9491
            },
            "lowestAvgFd": {
              "tripId": "6.T3.34-75-j20-2.1.H",
              "value": 0
            },
            "averaged_avg_fd": 61.23
          },
          "configuration": {
            "profile": "bus_shortest_custom",
            "candidate_search_radius": 25,
            "alpha": 25,
            "beta": 2,
            "u_turn_cost_penalty": 1500
          }
        }
      ]
    }
  }
}
</script>