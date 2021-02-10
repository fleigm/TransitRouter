<template>
  <div class="container">
    <ErrorCard v-for="(error, i) in errors" :key="i" :error="error" class="my-4"></ErrorCard>
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

  computed: {
    errors() {
      return this.info.errors.reverse();
    }
  }
}
</script>

<style scoped>

</style>