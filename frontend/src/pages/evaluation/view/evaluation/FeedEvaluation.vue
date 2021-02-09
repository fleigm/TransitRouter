<template>
  <div class="grid grid-cols-2 gap-4">
    <AccuracyEvaluationCard :feed="feed"></AccuracyEvaluationCard>
    <FrechetDistanceEvaluationCard :feed="feed" :report="report"></FrechetDistanceEvaluationCard>
    <AnEvaluationCard :feed="feed" :report="report"></AnEvaluationCard>
    <AlEvaluationCard :feed="feed" :report="report"></AlEvaluationCard>
  </div>
</template>

<script>
import HasEvaluation from "./HasEvaluation";
import AccuracyEvaluationCard from "./AccuracyEvaluationCard";
import FrechetDistanceEvaluationCard from "./FrechetDistanceEvaluationCard";
import AnEvaluationCard from "./AnEvaluationCard";
import AlEvaluationCard from "./AlEvaluationCard";

export default {
  name: "v-feed-evaluation",
  components: {AlEvaluationCard, AnEvaluationCard, FrechetDistanceEvaluationCard, AccuracyEvaluationCard},
  mixins: [HasEvaluation],

  data() {
    return {
      report: [],
    }
  },

/*  render() {
    return this.$scopedSlots.default({
      report: this.report,
    });
  },*/

  methods: {
    fetchReport() {
      const name = this.$route.params.name;
      this.$http.get(`feeds/${name}/report`)
          .then(({data}) => {
            this.report = data.entries;
          })
    }
  },

  mounted() {
    this.fetchReport();
  }

}
</script>

<style scoped>

</style>