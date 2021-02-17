<template>
  <div class="flex justify-between">
    <div class="grid grid-cols-2 gap-4">
      <div v-for="(params, type) in feed.parameters" :key="type">
        <p>{{ type | capitalize }}</p>
        <div class="flex">
          <v-metric :value="params.profile"
                    class=""
                    size="mini"
                    title="Profile"
          ></v-metric>
          <v-metric :value="params.useGraphHopperMapMatching ? 'GHMM' : 'TransitRouter'"
                    class=""
                    size="mini"
                    title="Router"
          ></v-metric>
        </div>
        <div class="flex gap-4">
          <v-metric :value="params.beta | number('0.00')"
                    class=""
                    size="mini"
                    title="beta"
          ></v-metric>
          <v-metric :value="params.sigma | number('0')"
                    class=""
                    size="mini"
                    title="sigma"
          ></v-metric>
          <v-metric :value="params.candidateSearchRadius | number('0')"
                    class=""
                    size="mini"
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