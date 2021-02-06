<template>
  <div class="flex-1">
<!--
    <div class="container flex justify-between items-center">
      <v-breadcrumbs></v-breadcrumbs>
      <div id="context-menu">
      </div>
    </div>
-->

    <router-view></router-view>
  </div>
</template>

<script>
import Breadcrumbs from "../../breadcrumbs";

export default {
  name: "Index",

  mounted() {
    Breadcrumbs.set({name: 'Feeds', route: { name: 'presets.index'}})
  }
}
</script>