<template>
  <div class="grid grid-cols-3">
    <div>
      <div>
        <div class="flex">
          <v-metric :value="feed.parameters.profile"
                    class=""
                    size="small"
                    title="Profile"
          ></v-metric>
          <v-metric :value="feed.parameters.useGraphHopperMapMatching ? 'GHMM' : 'TransitRouter'"
                    class=""
                    size="small"
                    title="Router"
          ></v-metric>
        </div>
        <div class="flex gap-4">
          <v-metric :value="feed.parameters.beta | number('0.00')"
                    class=""
                    title="beta"
          ></v-metric>
          <v-metric :value="feed.parameters.sigma | number('0')"
                    class=""
                    title="sigma"
          ></v-metric>
          <v-metric :value="feed.parameters.candidateSearchRadius | number('0')"
                    class=""
                    title="CSR"
          ></v-metric>
        </div>
      </div>
    </div>
    <div>
      <v-feed-evaluation-info :feed="feed"></v-feed-evaluation-info>
    </div>
  </div>
</template>

<script>
import VFeedEvaluationInfo from "./FeedEvaluationInfo";

export default {
  name: "v-feed-table-expand",
  components: {VFeedEvaluationInfo},
  props: {
    feed: {
      type: Object,
      required: true,
    },
  },
}
</script>

<style scoped>

</style>