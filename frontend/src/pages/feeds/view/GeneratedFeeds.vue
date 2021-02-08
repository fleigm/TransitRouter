<template>
  <div>
    <div v-if="loading" v-loading="true" class="w-full h-128"></div>
    <div v-else>

      <div class="my-8" v-show="feeds.pending.length">
        <div class="text-secondary">Currently generating feeds...</div>
        <div class="flex gap-x-4">
          <v-card v-for="feed in feeds.pending" :key="feed.id"
                  class="flex items-center gap-x-4 relative p-2 text-secondary">
            <div class="relative w-4">
              <v-spinner class="w-4 h-4"></v-spinner>
            </div>
            <div>{{ feed.name }}</div>
            <div class="">
              <div>{{ feed.parameters.profile }}</div>
              <div>
                {{ feed.parameters.sigma }} - {{ feed.parameters.beta }} -
                <span>{{ feed.parameters.useGraphHopperMapMatching ? 'GHMM' : 'TR' }}</span>
              </div>
            </div>
          </v-card>
        </div>
      </div>

      <v-reports :feeds="feeds.selected">
        <template slot-scope="reports">
          <div class="my-8 ">
            <div class="text-secondary">Generated Feeds</div>
            <div class="grid grid-cols-3 gap-4">
              <v-card class="">
                <el-table ref="finishedFeedsTable"
                          :data="feeds.evaluated"
                          size="mini"
                          height="440"
                          @selection-change="selectionChanged">
                  <el-table-column type="selection" width="30"></el-table-column>
                  <el-table-column prop="name" label="Name">
                    <template slot-scope="scope">
                      <span :style="{background: scope.row._color}"
                            class="px-2 rounded-full text-white inline-block"
                      >{{ scope.row.name }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="parameters">
                    <template slot-scope="scope">
                      <div class="">
                        <div>{{ scope.row.parameters.profile }}</div>
                        <div>
                          {{ scope.row.parameters.sigma }} - {{ scope.row.parameters.beta }} -
                          <span>{{ scope.row.parameters.useGraphHopperMapMatching ? 'GHMM' : 'TR' }}</span>
                        </div>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column width="40">
                    <template slot-scope="scope">
                      <el-popconfirm
                          cancel-button-text='No, Thanks'
                          confirm-button-text='Yes'
                          title="Are you sure to delete this feed?"
                          @confirm="deleteFeed(scope.row.id)">
                        <el-button slot="reference" icon="el-icon-delete" circle size="mini"></el-button>
                      </el-popconfirm>
                    </template>
                  </el-table-column>
                </el-table>
              </v-card>


              <template v-if="reports.reports.length">
                <v-card class="col-span-2" header="Average Frechet Distance">
                  <v-histogram class="relative m-2 h-128" :data-sets="reports.avgFrechetDistanceDataSets"
                               :options="reports.chartOptions"></v-histogram>
                </v-card>
                <v-card header="Accuracy">
                  <v-accuracy-chart class="relative m-2 h-128" :data-sets="reports.accuracyDataSets"
                                    :options="reports.chartOptions"></v-accuracy-chart>
                </v-card>
                <v-card header="percentage mismatched hop segments">
                  <v-histogram class="relative m-2 h-128" :data-sets="reports.anDataSets"
                               :options="reports.chartOptions"></v-histogram>
                </v-card>
                <v-card header="percentage length mismatched hop segments">
                  <v-histogram class="relative m-2 h-128" :data-sets="reports.alDataSets"
                               :options="reports.chartOptions"></v-histogram>
                </v-card>
              </template>
              <template v-else>
                <div class="text-xl font-thin text-secondary p-2">Select feeds to show evaluation results</div>
              </template>
            </div>
          </div>
        </template>
      </v-reports>

      <div class="flex">
        <v-card v-for="feed in feeds.finished" :key="feed.id"
                class="flex items-center gap-x-4 relative p-2 text-secondary">
          <div>{{ feed.name }}</div>
          <div class="">
            <div>{{ feed.parameters.profile }}</div>
            <div>
              {{ feed.parameters.sigma }} - {{ feed.parameters.beta }} -
              <span>{{ feed.parameters.useGraphHopperMapMatching ? 'GHMM' : 'TR' }}</span>
            </div>
          </div>
        </v-card>
      </div>
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
  hasEvaluation: (feed) => feed.extensions.hasOwnProperty('de.fleigm.ptmm.feeds.evaluation.Evaluation'),
}

const sortByDate = (a, b) => new Date(a.createdAt) - new Date(b.createdAt);

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
      feeds: {
        finished: [],
        evaluated: [],
        pending: [],
        failed: [],
        selected: [],
      },
      selectedFeeds: [],

      pollingPendingFeeds: null,
    }
  },

  computed: {},

  methods: {
    fetchStreams() {
      this.loading = true;
      this.$http.get(`presets/${this.presetId}/generated-feeds`)
          .then(({data}) => {
            data.filter(Filters.isFinished)
                .sort(sortByDate)
                .forEach(this.addFinishedFeed)

            this.feeds.pending = data.filter(Filters.isPending).sort(sortByDate);
            this.feeds.failed = data.filter(Filters.hasFailed).sort(sortByDate);

            this.$nextTick(this.showReportForFirstFeeds);
          })
          .finally(() => {
            this.loading = false;
          })
    },

    addFinishedFeed(feed) {
      if (Filters.hasEvaluation(feed)) {
        this.feeds.evaluated.push({
          ...feed,
          _color: colors[this.feeds.evaluated.length],
        });
      } else {
        this.feeds.finished.push(feed);
      }
    },

    showReportForFirstFeeds() {
      // hacky but otherwise $refs.finishedFeedsTable would be undefined
      // even though it is called inside $nextTick
      setTimeout(() => {
        this.feeds.evaluated.slice(0, 3).forEach((feed) => this.$refs.finishedFeedsTable.toggleRowSelection(feed));
      }, 100)
    },

    selectionChanged(selectedFeeds) {
      this.feeds.selected = selectedFeeds;
    }
    ,

    onGeneratedFeed(feed) {
      this.feeds.pending.push(feed);
    }
    ,

    async checkPendingFeeds() {
      if (this.feeds.pending.length === 0) {
        return;
      }

      const newlyFinished = [];
      for (const feed of this.feeds.pending) {
        const response = await this.$http.get(`feeds/${feed.id}`);
        console.log("incomming");
        if (response.data.status !== 'PENDING') {
          newlyFinished.push(response.data);
        }
      }

      if (newlyFinished.length === 0) {
        console.log('no new finished');
        return;
      }
      console.log("updated feeds");

      this.feeds.pending = this.feeds.pending.filter(feed => newlyFinished.findIndex(newFeed => feed.id === newFeed.id) < 0);
      newlyFinished.forEach(feed => {
        if (feed.status === 'FINISHED') {
          this.addFinishedFeed(feed);
        } else {
          this.feeds.failed.push(feed);
        }
      })
    }
    ,

    deleteFeed(id) {
      this.$http.delete(`feeds/${id}`)
          .then((response) => {
            this.$notify.success({
              title: 'Success.',
              message: 'Feed deleted.',
              duration: 5000,
            });
            const index = this.feeds.finished.findIndex(feed => feed.id === id);
            this.feeds.finished.splice(index, 1);
          })
          .catch((response) => {
            console.log(response);
            this.$notify.error({
              title: 'Oops, something went wrong',
              message: 'Could not delete feed.',
              duration: 5000,
            })
          })
    }
  },

  mounted() {
    this.fetchStreams();
    this.$events.$on('presets.generatedFeed', this.onGeneratedFeed);
    this.pollingPendingFeeds = setInterval(this.checkPendingFeeds, 10000);
  },

  beforeDestroy() {
    this.$events.$off('presets.generatedFeed', this.onGeneratedFeed);
    clearInterval(this.pollingPendingFeeds);
  },
}
</script>