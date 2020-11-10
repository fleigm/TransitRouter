<template>
  <div class="container">
    <el-breadcrumb separator-class="el-icon-arrow-right" class="my-8">
      <el-breadcrumb-item :to="{ name: 'evaluation.index' }">Evaluations</el-breadcrumb-item>
      <el-breadcrumb-item>{{ name }}</el-breadcrumb-item>
    </el-breadcrumb>
    <v-summary-card :name=name></v-summary-card>
    <v-card class="my-8">
      <v-report-list :name="name"></v-report-list>
    </v-card>

  </div>
</template>

<script>
import VReportList from "./ReportList";
import VSummaryCard from "./Summary";

export default {
  name: "Index",

  components: {VSummaryCard, VReportList},

  computed: {
    name() {
      return this.$route.params.name
    }
  }

}
</script>