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
            <el-breadcrumb-item :to="{ name: 'presets.index' }">Feeds</el-breadcrumb-item>
            <el-breadcrumb-item>{{ preset.name }}</el-breadcrumb-item>
          </el-breadcrumb>
          <div v-if="!notFound && !loading">
            <v-generate-feed-dialog :preset="preset"></v-generate-feed-dialog>

            <el-button size="mini">
              <el-link :underline="false" :href="downloadLink">
                Download<i class="el-icon-download el-icon--right"></i>
              </el-link>

            </el-button>

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

        <v-card>
          <FeedDetails :preset="preset"></FeedDetails>
        </v-card>
      </div>
    </v-promise>

    <div class="my-8">
      <v-preset-routing-index></v-preset-routing-index>
    </div>


    <div class="my-8">
      <GeneratedFeeds :preset-id="id"></GeneratedFeeds>
    </div>

  </div>
</template>

<script>
import Config from '../../../config';
import PresetService from "../PresetService";
import FeedDetails from "../overview/FeedDetails";
import VGenerateFeedDialog from "./GenerateFeedDialog";
import GeneratedFeeds from "./GeneratedFeeds";
import VPresetRoutingIndex from "./routing/Index";

export default {
  name: "Index",
  components: {VPresetRoutingIndex, GeneratedFeeds, VGenerateFeedDialog, FeedDetails},
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

    downloadLink() {
      return `${Config.apiEndpoint}/presets/${this.id}/download`;
    },
  },

  methods: {
    fetchPreset() {
      this.fetchPresetPromise = PresetService.fetchPreset(this.id);
    },

    deletePreset() {
      PresetService
          .deletePreset(this.id)
          .then(() => this.$router.push({name: 'presets.index'}))
    },

    download() {
      this.$http.get(`feeds/${this.id}/download`);
    }
  },

  mounted() {
    this.fetchPreset();
  }

}
</script>