<template>
  <v-card class="my-8" header="Summary">
    <div class="flex p-2">
      <div class="w-1/2">
        <div class="flex justify-center gap-4">
          <v-metric :value="info.statistics['highestAvgFd.value'] | number('0.00')"
                    class="text-red-400"
                    title="Highest avgFd"
                    unit="m"
          ></v-metric>
          <v-metric :value="info.statistics['lowestAvgFd.value'] | number('0.00')"
                    class="text-green-400"
                    title="Lowest avgFd"
                    unit="m"
          ></v-metric>
          <v-metric :value="info.statistics['averagedAvgFd'] | number('0.00')"
                    class=""
                    title="Average avgFd"
                    unit="m"
          ></v-metric>
        </div>
        <v-accuracy-chart :accuracies="info.statistics['accuracy']"
                          :height="150"
                          :width="300"></v-accuracy-chart>
      </div>
      <div class="w-1/2">
        <div class="flex justify-center gap-4 mb-2">
          <v-metric :value="info.parameters.alpha" size="small" title="alpha"></v-metric>
          <v-metric :value="info.parameters.beta" size="small" title="beta"></v-metric>
          <v-metric :value="info.parameters.candidateSearchRadius" size="small" title="csr"></v-metric>
          <v-metric :value="info.parameters.uTurnDistancePenalty" size="small"
                    title="uturn penalty"></v-metric>
        </div>

        <div class="flex justify-center gap-4 mb-2">
          <v-metric :value="info.parameters.profile" size="mini" title="Profile"></v-metric>
        </div>

        <div class="flex justify-center gap-4 mb-2">
          <v-metric :value="info.statistics.trips" size="small" title="Trips"></v-metric>
          <v-metric :value="info.statistics.generatedShapes" size="small" title="Shapes"></v-metric>
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
import VAccuracyChart from "../AccuracyChart";
import VRoutingResult from "../../dashboard/RoutingResult";
import VMap from "../../../components/Map";
import VMetric from "../Metric";

export default {
  name: "v-summary-card",

  components: {VMetric, VMap, VRoutingResult, VAccuracyChart},

  props: {
    info: {
      type: Object,
      required: true
    }
  },
}
</script>
