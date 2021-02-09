<template>
  <div>
    <div class="flex justify-between gap-4">
      <v-metric :value="evaluation.quickStats.accuracy[0] | number('0.00')"
                class=""
                title="Accuracy at 0"
                unit="%"
      ></v-metric>
      <v-metric :value="evaluation.quickStats.accuracy[2] | number('0.00')"
                class=""
                title="Accuracy at 20"
                unit="%"
      ></v-metric>
      <v-metric :value="evaluation.quickStats.accuracy[4] | number('0.00')"
                class=""
                title="Accuracy at 40"
                unit="%"
      ></v-metric>
    </div>
    <div class="flex justify-between gap-4">
      <v-metric :value="evaluation.quickStats.fd.average| number('0.00')"
                class=""
                title="Average Fd"
                unit="m"
      ></v-metric>
      <v-metric :value="evaluation.quickStats.an.average| number('0.00')"
                class=""
                title="Average an"
                unit="%"
      ></v-metric>
      <v-metric :value="evaluation.quickStats.al.average| number('0.00')"
                class=""
                title="Average al"
                unit="%"
      ></v-metric>
    </div>
  </div>
</template>

<script>
export default {
  name: "v-feed-evaluation-info",

  props: {
    feed: {
      type: Object,
      required: true,
    },
  },

  computed: {
    evaluation() {
      return this.feed.extensions['de.fleigm.ptmm.feeds.evaluation.Evaluation'];
    }
  }


}
</script>

<style scoped>

</style>