<template>
  <div class="container">
    <div class="flex justify-between items-center">
      <el-breadcrumb separator-class="el-icon-arrow-right" class="my-8">
        <el-breadcrumb-item :to="{ name: 'evaluation.index' }">Evaluations</el-breadcrumb-item>
        <el-breadcrumb-item>{{ name }}</el-breadcrumb-item>
      </el-breadcrumb>
      <div>
        <el-button plain type="danger" size="mini" @click="deleteEvaluation">Delete</el-button>
      </div>
    </div>

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
    },

    deleteEvaluation() {
      this.$http.delete(`eval/${this.name}`)
          .then(() => {
            this.$notify.success('Deleted evaluation ' + this.name + '.');
            this.$router.push({name: 'evaluation.index'})
          })
          .catch((error) => {
            this.$notify.error('Could not delete evaluation.');
            console.log(error);
          })
    }
  },

  mounted() {
    this.fetchInfo();
  }

}
</script>