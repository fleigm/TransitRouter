<template>
  <v-card class="my-8">
    <template #header>
      <div class="flex justify-between items-center p-2">
        <div class="flex gap-4">
          <router-link :to="{name: 'presets.view', params: {id: preset.id}}">
            {{ preset.name }}
          </router-link>
        </div>
        <span>{{ preset.createdAt | fromNow }}</span>
      </div>
    </template>
    <div>
      <FeedDetails :preset="preset"></FeedDetails>
    </div>
  </v-card>
</template>

<script>
import FeedDetails from "./FeedDetails";

export default {
  name: "v-preset-card",
  components: {FeedDetails},
  props: {
    preset: {
      type: Object,
      required: true,
    },
  },

  computed: {

  }


}
</script>

<style scoped>

</style>