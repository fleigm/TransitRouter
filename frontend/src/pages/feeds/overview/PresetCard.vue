<template>
  <v-card class="my-8">
    <template #header>
      <div class="flex justify-between items-center p-2">
        <div class="flex gap-4">
          <router-link :to="{name: 'preset.view', params: {name: preset.id}}">
            {{ preset.name }}
          </router-link>
        </div>
        <span>{{ preset.createdAt | fromNow }}</span>
      </div>
    </template>
  </v-card>
</template>

<script>
export default {
  name: "v-preset-card",

  props: {
    preset: {
      type: Object,
      required: true,
    },
  },


}
</script>

<style scoped>

</style>