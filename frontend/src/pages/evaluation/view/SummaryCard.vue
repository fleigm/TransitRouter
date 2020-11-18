<template>
  <v-card header="Summary">
    <v-summary :info="info"></v-summary>
  </v-card>
</template>

<script>
import VSummary from "./Summary";
export default {
  name: "v-summary-card",
  components: {VSummary},
  props: {
    info: {
      type: Object,
      required: true
    }
  },
}
</script>
