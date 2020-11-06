<template>
  <div>
    <el-progress :percentage="event.progress"></el-progress>
  </div>
</template>

<script>
export default {
  name: "UploadProgressNotification",

  props: {
    event: {
      type: Object,
      required: true,
    }
  }
}
</script>