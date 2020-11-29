<template>
  <div class="w-full">
    <div class="container flex justify-between items-center">
      <el-breadcrumb separator-class="el-icon-arrow-right" class="my-8">
        <el-breadcrumb-item :to="{ name: 'evaluation.index' }">Evaluations</el-breadcrumb-item>
        <el-breadcrumb-item>{{ name }}</el-breadcrumb-item>
      </el-breadcrumb>
      <div>
        <el-dropdown>
          <el-button size="mini">
            Download<i class="el-icon-arrow-down el-icon--right"></i>
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item>
              <el-link :underline="false" :href="downloadLinkGeneratedFeed">Generated GTFS Feed</el-link>
            </el-dropdown-item>
            <el-dropdown-item>
              <el-link :underline="false" :href="downloadLinkFull">All Files</el-link>
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>

        <el-button plain type="danger" size="mini" @click="deleteEvaluation">Delete</el-button>
      </div>
    </div>

    <v-finished-view v-if="finished" :info="info"></v-finished-view>
    <v-failed-view v-else-if="failed" :info="info"></v-failed-view>

  </div>
</template>

<script>
import VReportList from "./ReportList";
import VFinishedView from "./FinishedView";
import VFailedView from "./FailedView";
import EvaluationService from "../EvaluationService";

export default {
  name: "Index",

  components: {VFailedView, VFinishedView, VReportList},

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
    },

    downloadLinkFull() {
      return `eval/${this.name}/download`;
    },
    downloadLinkGeneratedFeed() {
      return `eval/${this.name}/download/generated`;
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
      EvaluationService.deleteEvaluation(this.evaluation.name);
    },

    download() {
      this.$http.get(`eval/${this.name}/download`);
    }
  },

  mounted() {
    this.fetchInfo();
  }

}
</script>