<template>
  <div>
    <div v-if="loading" v-loading="true" class="w-full h-128"></div>
    <div v-else>
      <div class="text-secondary">Generated Feeds</div>
      <v-card class="">
        <el-table ref="finishedFeedsTable"
                  :data="feeds"
                  empty-text="No generated feeds from this preset yet"
                  size="mini"
                  max-height="440"
                  :default-sort="{prop: 'createdAt', order: 'descending'}"
                  @selection-change="selectionChanged">
          <el-table-column type="selection" width="55"></el-table-column>
          <el-table-column prop="name" label="Name" sortable width="200"></el-table-column>

          <el-table-column label="Parameters" width="200">
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

          <el-table-column label="Status" prop="status" sortable width="100"
                           :filters="statusFilter"
                           :filter-method="filterByStatus">
            <template slot-scope="scope">
              <v-feed-status-tag :feed="scope.row"></v-feed-status-tag>
            </template>
          </el-table-column>

          <el-table-column label="Extensions">
            <template slot-scope="scope">
              <div>
                <v-feed-extension-tag v-for="(ext, name) in scope.row.extensions" :key="`${scope.row.id}:${name}`"
                                      :extension="name"></v-feed-extension-tag>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="Created" width="150" prop="createdAt" sortable>
            <template slot-scope="scope">
              <i class="el-icon-time"></i>
              <span style="margin-left: 10px">{{ scope.row.createdAt | fromNow }}</span>
            </template>
          </el-table-column>

          <el-table-column width="" align="right">
            <template slot="header" slot-scope="scope">
              <el-button @click="showEvaluationOfSelectedFeeds" :disabled="!selectedFeeds.length" icon="el-icon-s-data" circle size="mini"></el-button>
              <el-popconfirm cancel-button-text='No, Thanks'
                             confirm-button-text='Yes'
                             title="Are you sure to delete all selected feeds?"
                             @confirm="deleteSelectedFeeds">
                <el-button slot="reference" :disabled="!selectedFeeds.length" icon="el-icon-delete" circle size="mini"></el-button>
              </el-popconfirm>
            </template>
            <template slot-scope="scope">
              <el-button @click="showEvaluation(scope.row)"
                         :disabled="!scope.row.extensions['de.fleigm.transitrouter.feeds.evaluation.Evaluation'] || scope.row.status === 'PENDING'"
                         icon="el-icon-s-data" circle size="mini" title="show evaluation"></el-button>
              <router-link :to="{name: 'evaluation.view', params: {name: scope.row.id}}">
                <el-button icon="el-icon-right" circle size="mini" title="go to feed"></el-button>
              </router-link>
              <el-popconfirm cancel-button-text='No, Thanks'
                             confirm-button-text='Yes'
                             title="Are you sure to delete this feed?"
                             @confirm="deleteFeed(scope.row.id)">
                <el-button slot="reference" icon="el-icon-delete" circle size="mini" :disabled="scope.row.status === 'PENDING'"></el-button>
              </el-popconfirm>
            </template>
          </el-table-column>

        </el-table>
      </v-card>

      <v-feed-evaluations></v-feed-evaluations>

    </div>
  </div>
</template>

<script>
import VReports from "./Reports";
import VAccuracyChart from "./AccuracyChart";
import VFeedStatusTag from "./FeedStatusTag";
import VFeedExtensionTag from "./FeedExtensionTag";
import VFeedEvaluations from "./FeedEvaluations";

const Filters = {
  isFinished: (feed) => feed.status === 'FINISHED',
  isPending: (feed) => feed.status === 'PENDING',
  hasFailed: (feed) => feed.status === 'FAILED',
  hasEvaluation: (feed) => feed.extensions.hasOwnProperty('de.fleigm.transitrouter.feeds.evaluation.Evaluation'),
}

const sortByDate = (a, b) => new Date(a.createdAt) - new Date(b.createdAt);


export default {
  name: "GeneratedFeeds",
  components: {VFeedEvaluations, VFeedExtensionTag, VFeedStatusTag, VAccuracyChart, VReports},
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

      statusFilter: [
        {text: 'finished', value: 'FINISHED'},
        {text: 'failed', value: 'FAILED'},
        {text: 'pending', value: 'PENDING'},
      ],

      pollingPendingFeeds: null,
    }
  },

  computed: {},

  methods: {
    fetchStreams() {
      this.loading = true;
      this.$http.get(`presets/${this.presetId}/generated-feeds`)
          .then(({data}) => {
            this.feeds = data;
            setTimeout(this.showEvaluationOfFirstFeeds, 100);
          })
          .finally(() => {
            this.loading = false;
          })
    },

    onGeneratedFeed(feed) {
      this.feeds.push(feed);
    },

    filterByStatus(value, row) {
      return row.status === value;
    },

    selectionChanged(selectedFeeds) {
      this.selectedFeeds = selectedFeeds;
    },

    deleteSelectedFeeds() {
      this.selectedFeeds
          .filter(feed => feed.status !== 'PENDING')
          .forEach(feed => this.deleteFeed(feed.id))
    },

    showEvaluationOfSelectedFeeds() {
      this.selectedFeeds
          .filter(Filters.hasEvaluation)
          .forEach(this.showEvaluation);
    },

    showEvaluationOfFirstFeeds() {
      this.feeds
          .filter(Filters.isFinished)
          .filter(Filters.hasEvaluation)
          .sort(sortByDate)
          .slice(0, 3)
          .forEach(this.showEvaluation)
    },

    showEvaluation(feed) {
      this.$events.$emit('FeedEvaluations:show', feed);
    },

    checkPendingFeeds() {
      this.feeds.filter(Filters.isPending).forEach(this.updateFeed);
    },

    updateFeed(feed) {
      this.$http.get(`feeds/${feed.id}`).then(({data}) => {
        if (data.status !== 'PENDING') {
          let index = this.feeds.findIndex(f => f.id === feed.id);
          if (index >= 0) {
            this.feeds.splice(index, 1, data);
          }
        }
      });

    },

    deleteFeed(id) {
      this.$http.delete(`feeds/${id}`)
          .then((response) => {
            this.$notify.success({
              title: 'Success.',
              message: 'Feed deleted.',
              duration: 5000,
            });
            const index = this.feeds.findIndex(feed => feed.id === id);
            this.feeds.splice(index, 1);
            this.$events.$emit('FeedEvaluations:hide', {id});
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