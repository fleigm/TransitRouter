<template>

  <Button class="p-button-outlined p-button-secondary" @click="open">
    <span>
    Evaluate GTFS Feed <i class="el-icon-upload el-icon-right"></i>
    </span>
  </Button>
  <Portal target="portal-target">
    <Dialog
        v-model:visible="showDialog"
        header="Upload GTFS feed for evaluation"
        :modal="true">
      <v-evaluation-form></v-evaluation-form>
    </Dialog>
  </Portal>
</template>

<script>
import VEvaluationForm from "./EvaluationForm";

export default {
  name: "v-evaluation-form-dialog",

  components: {VEvaluationForm},

  data() {
    return {
      showDialog: true,
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
    this.$events.on('evaluation:createdRequest', this.close)
  },

  beforeDestroy() {
    this.$events.off('evaluation:createdRequest', this.close)
  }
}
</script>
