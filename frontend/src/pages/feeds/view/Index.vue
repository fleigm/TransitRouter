<template>
  <div class="container">

    <v-promise :promise="fetchPresetPromise">
      <div slot="pending" v-loading="true" class="w-full h-128"></div>
      <div slot="error" class="text-secondary text-2xl font-thin text-center py-8">
        Could not find feed with id {{ id }}
      </div>

      <div slot-scope="{data : preset}">
        <div class="flex justify-between items-center">
          <el-breadcrumb separator-class="el-icon-arrow-right" class="my-8">
            <el-breadcrumb-item :to="{ name: 'feeds.index' }">Feeds</el-breadcrumb-item>
            <el-breadcrumb-item>{{ preset.name }}</el-breadcrumb-item>
          </el-breadcrumb>
          <div v-if="!notFound && !loading">
            <v-generate-feed-dialog :preset="preset"></v-generate-feed-dialog>
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

            <el-popconfirm
                cancel-button-text='No, Thanks'
                confirm-button-text='Yes'
                title="Are you sure to delete this feed?"
                @confirm="deletePreset()"
            >
              <el-button slot="reference" plain size="mini" type="danger">Delete</el-button>
            </el-popconfirm>
          </div>
        </div>

        <FeedDetails :preset="preset"></FeedDetails>
      </div>
    </v-promise>


    <div class="my-8">
      <GeneratedFeeds :preset-id="id"></GeneratedFeeds>
    </div>

  </div>
</template>

<script>
import PresetService from "../PresetService";
import FeedDetails from "../overview/FeedDetails";
import VGenerateFeedDialog from "./GenerateFeedDialog";
import GeneratedFeeds from "./GeneratedFeeds";
import PresetFeeds from "../PresetFeeds";

export default {
  name: "Index",
  components: {GeneratedFeeds, VGenerateFeedDialog, FeedDetails},
  data() {
    return {
      loading: false,
      preset: {
        name: '',
      },
      notFound: false,
      fetchPresetPromise: null,
    }
  },

  computed: {
    id() {
      return this.$route.params.id
    },

    presetFeeds() {
      return PresetFeeds.state
    },

    downloadLinkFull() {
      return `eval/${this.id}/download`;
    },

    downloadLinkGeneratedFeed() {
      return `eval/${this.id}/download/generated`;
    }
  },

  methods: {
    fetchPreset() {
      this.fetchPresetPromise = PresetService.fetchPreset(this.id);
    },

    deletePreset() {
      PresetService
          .deleteEvaluation(this.id)
          .then(() => this.$router.push({name: 'feeds.index'}))
    },

    download() {
      this.$http.get(`eval/${this.id}/download`);
    }
  },

  mounted() {
    this.fetchPreset();
    PresetFeeds.fetchFeeds(this.id);
  }

}
</script>