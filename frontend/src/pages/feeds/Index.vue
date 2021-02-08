<template>
  <div class="flex-1">
    <!--
        <div class="container flex justify-between items-center">
          <v-breadcrumbs></v-breadcrumbs>
          <div id="context-menu">
          </div>
        </div>
    -->

    <transition enter-active-class="transition-all transition-opacity duration-150"
                enter-from-class="opacity-0"
                enter-to-class="opacity-100"
                leave-active-class="transition-all transition-opacity duration-150"
                leave-from-class="opacity-100"
                leave-to-class="opacity-0">
      <router-view class="bg-white"></router-view>
    </transition>
  </div>
</template>

<script>
import Breadcrumbs from "../../breadcrumbs";

export default {
  name: "Index",

  mounted() {
    Breadcrumbs.set({name: 'Feeds', route: {name: 'presets.index'}})
  }
}
</script>