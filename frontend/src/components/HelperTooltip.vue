<template>
  <el-popover
      :title="title"
      trigger="hover">
    <i slot="reference" class="el-icon-question text-tertiary"></i>
    <slot>{{ content }}</slot>
  </el-popover>
</template>

<script>
export default {
  name: "HelperTooltip",

  props: {
    title: String,
    content: String,
  }
}
</script>