<template>
  <v-card header="evaluation error message">
    <div class="p-2">
      <pre class="py-2">{{ info.extension['error.message']}}</pre>
      <pre class="py-2">{{ info.extension['error.rootCause']}}</pre>
      <pre class="py-2">{{ info.extension['error.stackTrace'] }}</pre>
    </div>
  </v-card>
</template>

<script>
export default {
  name: "v-failed-view",

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