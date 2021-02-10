<template>
  <el-tag size="mini">{{ text }}</el-tag>
</template>

<script>
const extensions = {
  'de.fleigm.ptmm.feeds.evaluation.Evaluation': {
    text: 'eval',
  },
  'de.fleigm.ptmm.gtfs.FeedDetails': {
    text: 'info',
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
    extensions() {
      return extensions[this.extension] || {text: ''}
    },

    text() {
      return this.extensions.text
    }


  }
}
</script>

<style scoped>

</style>