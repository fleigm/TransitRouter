<template>
  <div class="container">
    <div class="my-8 flex justify-between">
      <div></div>
      <div class="">
        <el-button size="mini" @click="clearCache">Clear Cache</el-button>
        <v-generate-feed-dialog></v-generate-feed-dialog>
      </div>
    </div>

    <v-card>
      <el-table ref="feedsTable" :data="feeds" size="mini" :default-expand-all="false" v-loading="loadingFeeds" stripe>
        <el-table-column type="expand">
          <template slot-scope="scope">
            <v-feed-table-expand :feed="scope.row"></v-feed-table-expand>
          </template>
        </el-table-column>

        <el-table-column label="Name" prop="name" width="200"></el-table-column>
        <el-table-column label="Parameters" props="parameters">
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

        <el-table-column label="Status" prop="status" sortable width="100">
          <template slot-scope="scope">
            <v-feed-status-tag :feed="scope.row"></v-feed-status-tag>
          </template>
        </el-table-column>

        <el-table-column label="Extensions" width="100">
          <template slot-scope="scope">
            <div>
              <v-feed-extension-tag v-for="(ext, name) in scope.row.extensions"
                                    :key="`${scope.row.id}:${name}`"
                                    :extension="name">
              </v-feed-extension-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="Created" width="150" prop="createdAt" sortable>
          <template slot-scope="scope">
            <i class="el-icon-time"></i>
            <span style="margin-left: 10px">{{ scope.row.createdAt | fromNow }}</span>
          </template>
        </el-table-column>

        <el-table-column width="100" align="right">
          <template slot-scope="scope">
            <router-link :to="{name: 'evaluation.view', params: {name: scope.row.id}}">
              <el-button icon="el-icon-right" circle size="mini" title="go to feed"></el-button>
            </router-link>
            <el-popconfirm cancel-button-text='No, Thanks'
                           confirm-button-text='Yes'
                           title="Are you sure to delete this feed?"
                           @confirm="deleteFeed(scope.row.id)">
              <el-button slot="reference" icon="el-icon-delete" circle size="mini"></el-button>
            </el-popconfirm>
          </template>
        </el-table-column>

      </el-table>
    </v-card>

    <div v-if="!feeds.length">
      <div class="text-secondary text-2xl font-thin text-center py-8">There are currently no evaluations.</div>
    </div>
  </div>
</template>

<script>
import VGenerateFeedForm from "./GenerateFeedForm";
import VGenerateFeedDialog from "./GenerateFeedDialog";
import GeneratedFeedService from "../GeneratedFeedService";
import VFeedStatusTag from "../../feeds/view/FeedStatusTag";
import VFeedExtensionTag from "../../feeds/view/FeedExtensionTag";
import VFeedTableExpand from "./FeedTableExpand";
import VFeedEvaluationInfo from "./FeedEvaluationInfo";

export default {
  name: "Overview",

  components: {
    VFeedEvaluationInfo,
    VFeedTableExpand,
    VFeedExtensionTag,
    VFeedStatusTag,
    VGenerateFeedDialog,
    VGenerateFeedForm
  },

  computed: {
    feeds() {
      return GeneratedFeedService.state.evaluations;
    },
    loadingFeeds() {
      return GeneratedFeedService.state.loading;
    }
  },

  methods: {
    clearCache() {
      GeneratedFeedService.clearCache()
    },

    deleteFeed(id) {
      GeneratedFeedService.deleteEvaluation(id);
    }
  },

  mounted() {
    GeneratedFeedService.fetchEvaluations();
  }
}
</script>