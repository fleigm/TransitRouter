<template>
  <v-card>
    <div slot="header" class="p-2 flex justify-between">
      <span class="text-xl font-thin">{{ error.message }}</span>
      <el-tag size="mini">{{ error.code }}</el-tag>
    </div>
    <div class="p-2 relative">
      <el-collapse>
        <el-collapse-item>
          <div slot="title" class="text-secondary">Details:</div>
          <div class="border rounded bg-gray-100 p-2">
            <div class="whitespace-pre-wrap">{{ error.details }}</div>
          </div>
        </el-collapse-item>

        <el-collapse-item>
          <div slot="title" class="text-secondary">Exception:</div>
          <div class="border rounded bg-gray-100 p-2">
            <div class="whitespace-pre-wrap">{{ error.exception }}</div>
          </div>
        </el-collapse-item>
      </el-collapse>

    </div>
  </v-card>
</template>

<script>
export default {
  name: "ErrorCard",

  props: {
    error: {
      type: Object,
      required: true,
    }
  }
}
</script>

<style scoped>

</style>