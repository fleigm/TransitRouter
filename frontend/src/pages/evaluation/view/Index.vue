<template>
  <div class="container">
    <el-breadcrumb separator-class="el-icon-arrow-right" class="my-8">
      <el-breadcrumb-item :to="{ name: 'evaluation.index' }">Evaluations</el-breadcrumb-item>
      <el-breadcrumb-item>{{ name }}</el-breadcrumb-item>
    </el-breadcrumb>

    <v-finished-view v-if="finished" :info="info"></v-finished-view>
    <v-failed-view v-else-if="failed" :info="info"></v-failed-view>

  </div>
</template>

<script>
import VReportList from "./ReportList";
import VSummaryCard from "./Summary";
import VFinishedView from "./FinishedView";
import VFailedView from "./FailedView";

export default {
  name: "Index",

  components: {VFailedView, VFinishedView, VSummaryCard, VReportList},

  data() {
    return {
      loading: false,
      info: null,
    }
  },

  computed: {
    name() {
      return this.$route.params.name
    },

    finished() {
      return this.info && this.info.status === 'FINISHED';
    },

    failed() {
      return this.info && this.info.status === 'FAILED';
    }

  },

  methods: {
    fetchInfo() {
      this.loading = true;
      this.$http.get(`eval/${this.name}`)
          .then(({data}) => {
            this.info = data;
          })
          .finally(() => {
            this.loading = false;
          })
    }
  },

  mounted() {
    this.fetchInfo();
  }

}
</script>