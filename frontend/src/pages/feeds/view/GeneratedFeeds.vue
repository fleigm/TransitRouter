<template>
  <div>
    <div v-if="loading" v-loading="true" class="w-full h-128"></div>
    <div v-else>

      <v-reports :feeds="selectedFeeds">
        <template slot-scope="reports">
          <div class="my-8 grid grid-cols-3 gap-4">
            <v-card class="">
              <el-table :data="finishedFeeds" size="mini" @selection-change="selectionChanged">
                <el-table-column type="selection" width="30"></el-table-column>
                <el-table-column prop="name" label="Name">
                  <template slot-scope="scope">
                  <span :style="{background: scope.row._color}" class="px-2 rounded-full text-white inline-block">{{
                      scope.row.name
                    }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="parameters">
                  <template slot-scope="scope">
                    <div class="">
                      <div>{{ scope.row.parameters.profile }}</div>
                      <div>{{ scope.row.parameters.sigma }} - {{ scope.row.parameters.beta }}</div>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
            </v-card>

            <template v-if="reports.reports.length">
              <v-card class="col-span-2" header="Average Frechet Distance">
                <v-histogram class="relative m-2 h-128" :data-sets="reports.avgFrechetDistanceDataSets" :options="reports.chartOptions"></v-histogram>
              </v-card>
              <v-card header="Accuracy">
                <v-accuracy-chart class="relative m-2 h-128" :data-sets="reports.accuracyDataSets" :options="reports.chartOptions"></v-accuracy-chart>
              </v-card>
              <v-card header="percentage mismatched hop segments">
                <v-histogram class="relative m-2 h-128" :data-sets="reports.anDataSets" :options="reports.chartOptions"></v-histogram>
              </v-card>
              <v-card header="percentage length mismatched hop segments">
                <v-histogram class="relative m-2 h-128" :data-sets="reports.alDataSets" :options="reports.chartOptions"></v-histogram>
              </v-card>
            </template>
          </div>

        </template>
      </v-reports>
    </div>
  </div>
</template>

<script>
import VReports from "./Reports";
import VAccuracyChart from "./AccuracyChart";

const Filters = {
  isFinished: (feed) => feed.status === 'FINISHED',
  isPending: (feed) => feed.status === 'PENDING',
  hasFailed: (feed) => feed.status === 'FAILED',
  hasEvaluation: (feed) => feed.extensions.hasOwnProperty('de.fleigm.ptmm.eval.EvaluationExtension'),
}

const colors = ['#1E3A8A', '#3B82F6', '#D97706', '#F59E0B', '#991B1B', '#EF4444', '#064E3B', '#059669', '#4F46E5', '#7C3AED'];

export default {
  name: "GeneratedFeeds",
  components: {VAccuracyChart, VReports},
  props: {
    presetId: {
      required: true,
    }
  },

  data() {
    return {
      loading: false,
      feeds: [],
      selectedFeeds: [],
    }
  },

  computed: {
    finishedFeeds() {
      return this.feeds.filter(Filters.isFinished)
    },
  },


  methods: {
    fetchStreams() {
      this.loading = true;
      this.$http.get(`presets/${this.presetId}/generated-feeds`)
          .then(({data}) => {
            this.feeds = data.map((x, i) => {
              return {
                ...x,
                _color: colors[i],
              }
            })
          })
          .finally(() => {
            this.loading = false;
          })
    },

    selectionChanged(selectedFeeds) {
      this.selectedFeeds = selectedFeeds;
    },

    selectColor(number) {
      const hue = number * 137.508; // use golden angle approximation
      return `hsl(${hue},50%,75%)`;
    }


  },

  mounted() {
    this.fetchStreams();
  }
}
</script>