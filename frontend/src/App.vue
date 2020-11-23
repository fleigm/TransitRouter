<template>
  <div class="h-full">
    <div class="flex justify-between items-center border-b px-4">
      <div class="">
        <div class="text-xl font-thin">Public Transit Map Matching</div>
      </div>
      <v-navigation></v-navigation>
    </div>
    <div class="flex-1">
      <router-view></router-view>
    </div>
  </div>
</template>

<script>
import VNavigation from "./partials/Navigation";

export default {
  name: "App",

  components: {VNavigation},
}
</script>
