<template>
  <v-promise :promise="promise">
    <template slot-scope="{ data }">
      <slot :data="data"></slot>
    </template>
    <template #pending>
      <div v-loading="true" class="h-full w-full"></div>
    </template>

  </v-promise>
</template>

<script>
export default {
  name: "v-get",

  props: {
    resource: {
      type: String,
      required: true,
    },
  },

  computed: {
    promise() {
      return this.$http.get(this.resource);
    }
  }
}
</script>

<style scoped>

</style>