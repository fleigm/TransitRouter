<template>
  <el-tag v-if="ext" size="mini">{{ text }}</el-tag>
</template>

<script>
const extensions = {
  'de.fleigm.transitrouter.feeds.evaluation.Evaluation': {
    text: 'eval',
  },
  'de.fleigm.transitrouter.gtfs.FeedDetails': {
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
    ext() {
      return extensions[this.extension]
    },

    text() {
      return this.ext.text
    }


  }
}
</script>

<style scoped>

</style>