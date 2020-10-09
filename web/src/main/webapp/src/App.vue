<template>
  <el-container class="h-full">
    <el-header class="flex items-center border-b">
      <el-container class="">
        <h1>Public Transit Map Matching</h1>
      </el-container>
      <v-navigation></v-navigation>
    </el-header>
    <el-container class="flex-1">
      <router-view></router-view>
    </el-container>
  </el-container>
</template>

<script>
import VNavigation from "./partials/Navigation";

export default {
  name: "App",
  components: {VNavigation},
}
</script>
