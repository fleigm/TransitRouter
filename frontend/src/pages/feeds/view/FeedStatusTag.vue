<template>
  <el-tag :type="type" size="mini">{{ text }}</el-tag>
</template>

<script>
const statusType = {
  'PENDING': {
    tag: 'info',
    text: 'pending',
  },
  'FINISHED': {
    tag: 'success',
    text: 'finished',
  },
  'FAILED': {
    tag: 'danger',
    text: 'failed',
  },
};
export default {
  name: "v-feed-status-tag",

  props: {
    feed: {
      type: Object,
      required: true,
    },
  },

  computed: {
    type() {
      return statusType[this.feed.status].tag;
    },

    text() {
      return statusType[this.feed.status].text
    }


  }
}
</script>

<style scoped>

</style>