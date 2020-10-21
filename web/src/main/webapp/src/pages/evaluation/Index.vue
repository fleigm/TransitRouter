<template>
  <div class="container py-12">
    <v-card class="">
      <v-report-list name="stuttgart"></v-report-list>
    </v-card>
  </div>
</template>

<script>
import VReportList from "./ReportList";
export default {
  name: "Index",
  components: {VReportList},
  data() {
    return {
      query: "",
    }
  },

  methods: {
    sort(a) {
      console.log(a);
    }
  },
}
</script>

<style scoped>

</style>