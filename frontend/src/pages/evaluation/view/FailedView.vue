<template>
  <div class="container">
    <ErrorCard v-for="(error, i) in info.errors" :key="i" :error="error"></ErrorCard>
  </div>
</template>

<script>
import EvaluationCard from "./EvaluationCard";
import ErrorCard from "./ErrorCard";

export default {
  name: "v-failed-view",
  components: {ErrorCard, EvaluationCard},
  props: {
    info: {
      type: Object,
      required: true,
    }
  },
}
</script>

<style scoped>

</style>