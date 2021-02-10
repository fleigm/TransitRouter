<template>

  <el-button plain size="mini" type="primary" @click="open">
    <span>
    Evaluate GTFS Feed <i class="el-icon-upload el-icon-right"></i>
    </span>

    <portal>
      <el-dialog
          :visible.sync="showDialog"
          title="Upload GTFS feed for evaluation">

        <v-generate-feed-form></v-generate-feed-form>
      </el-dialog>
    </portal>
  </el-button>
</template>

<script>
import VGenerateFeedForm from "./GenerateFeedForm";

export default {
  name: "v-generate-feed-dialog",

  components: {VGenerateFeedForm},

  data() {
    return {
      showDialog: false,
    }
  },

  methods: {
    open() {
      this.showDialog = true;
    },
    close() {
      this.showDialog = false;
    }
  },

  mounted() {
    this.$events.$on('evaluation:createdRequest', this.close)
  },

  beforeDestroy() {
    this.$events.$off('evaluation:createdRequest', this.close)
  }
}
</script>
