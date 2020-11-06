<template>

  <el-button plain size="mini" type="primary" @click="open">
    <span>
    Evaluate GTFS Feed <i class="el-icon-upload el-icon-right"></i>
    </span>

    <portal>
      <el-dialog
          :visible.sync="showDialog"
          title="Upload GTFS feed for evaluation">

        <v-evaluation-form></v-evaluation-form>
      </el-dialog>
    </portal>
  </el-button>
</template>

<script>
import VEvaluationForm from "./EvaluationForm";

export default {
  name: "v-evaluation-form-dialog",

  components: {VEvaluationForm},

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
