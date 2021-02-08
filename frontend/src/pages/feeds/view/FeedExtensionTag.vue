<template>
  <el-tag size="mini">{{ text }}</el-tag>
</template>

<script>
const extensions = {
  'de.fleigm.ptmm.feeds.evaluation.Evaluation': {
    text: 'eval',
  }
};
export default {
  name: "v-feed-extension-tag",

  props: {
    extension: {
      type: String,
      required: true
    }
  },

  computed: {
    type() {
      return extensions[this.extension].tag;
    },

    text() {
      return extensions[this.extension].text
    }


  }
}
</script>

<style scoped>

</style>