<template>
  <div class="flex flex-col gap-4 p-2">
    <div class="flex justify-between gap-4">
      <v-metric :value="evaluation.quickStats.fd.max | number('0.00')"
                class="text-red-400"
                title="Highest avgFd"
                unit="m"
      ></v-metric>
      <v-metric :value="evaluation.quickStats.fd.min | number('0.00')"
                class="text-green-400"
                title="Lowest avgFd"
                unit="m"
      ></v-metric>
      <v-metric :value="evaluation.quickStats.fd.average| number('0.00')"
                class=""
                title="Average avgFd"
                unit="m"
      ></v-metric>
      <v-metric :value="shapeGenerationErrors.length"
                class=""
                title="shape generation errors"
                unit=""
      ></v-metric>
      <v-metric :value="affectedTripCount"
                class=""
                title="affected trips"
                unit=""
      ></v-metric>
    </div>
    <div class="flex">
      <div class="w-1/2">

        <div class="">
          <div class="py-2 flex">
            <div class="mr-4">acc-0:</div>
            <div>{{ evaluation.quickStats.accuracy[0] }}</div>
          </div>
          <div class="py-2 flex">
            <div class="mr-4">acc-10:</div>
            <div>{{ evaluation.quickStats.accuracy[1] }}</div>
          </div>
          <div class="py-2 flex">
            <div class="mr-4">acc-20:</div>
            <div>{{ evaluation.quickStats.accuracy[2] }}</div>
          </div>
          <div class="py-2 flex">
            <div class="mr-4">acc-30:</div>
            <div>{{ evaluation.quickStats.accuracy[3] }}</div>
          </div>
          <div class="py-2 flex">
            <div class="mr-4">acc-40:</div>
            <div>{{ evaluation.quickStats.accuracy[4] }}</div>
          </div>
        </div>

<!--        <v-accuracy-chart :accuracies="info.statistics['accuracy']" class="h-64 relative"></v-accuracy-chart>-->
      </div>
      <div class="w-1/2">
        <div class="flex justify-center gap-4 mb-2">
          <v-metric :value="info.parameters.sigma" size="small" title="sigma"></v-metric>
          <v-metric :value="info.parameters.beta" size="small" title="beta"></v-metric>
          <v-metric :value="info.parameters.candidateSearchRadius" size="small" title="csr"></v-metric>
        </div>

        <div class="flex justify-center gap-4 mb-2">
          <v-metric :value="info.parameters.profile" size="mini" title="Profile"></v-metric>
          <v-metric :value="info.statistics.trips" size="small" title="Trips"></v-metric>
          <v-metric :value="info.statistics.generatedShapes" size="small" title="Shapes"></v-metric>
        </div>

        <div class="flex justify-center gap-4 mb-2">
          <v-metric :value="info.statistics['executionTime.total'] / 1000 | number('0')"
                    size="small"
                    title="Total"
                    unit="s"
          ></v-metric>
          <v-metric :value="info.statistics['executionTime.shapeGeneration'] / 1000 | number('0')"
                    size="small"
                    title="Shape generation"
                    unit="s"
          ></v-metric>
          <v-metric :value="info.statistics['executionTime.evaluation'] / 1000 | number('0')"
                    size="small"
                    title="Evaluation"
                    unit="s"
          ></v-metric>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import VAccuracyChart from "../AccuracyChart";
import VMap from "../../../components/Map";

export default {
  name: "v-summary",

  components: {VMap, VAccuracyChart},

  props: {
    info: {
      type: Object,
      required: true
    }
  },

  computed: {
    evaluation() {
      return this.info.extensions['de.fleigm.ptmm.feeds.evaluation.Evaluation'];
    },

    shapeGenerationErrors() {
      return this.info.errors.filter(error => error.code === 'shape_generation_failed') ?? [];
    },
    affectedTripCount() {
      return this.shapeGenerationErrors
          .flatMap(error => error.details.trips.length)
          .reduce((sum, count) => sum + count, 0)
    }
  }
}
</script>
