<template>
  <v-card class="my-8" header="Summary">
    <v-promise :promise="fetch">
      <template #default="{data : evaluation}">
        <div class="flex p-2">
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
      </template>
      <template #pending>
        <div v-loading="true" class="h-64 w-full"></div>
      </template>
    </v-promise>
  </v-card>
</template>

<script>
import VAccuracyChart from "../AccuracyChart";
import VRoutingResult from "../../dashboard/RoutingResult";
import VMap from "../../../components/Map";
import VMetric from "../Metric";

export default {
  name: "v-summary-card",

  components: {VMetric, VMap, VRoutingResult, VAccuracyChart},

  props: {
    name: {
      type: String,
      required: true
    }
  },

  data() {
    return {
      fetch: this.$http.get(`eval/${this.name}`),
    }
  }
}
</script>
