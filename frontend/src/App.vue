<template>
  <el-container class="h-full">
    <el-header class="border-b">
      <div class="container flex items-center ">
        <el-container class="">
          <h1>Public Transit Map Matching</h1>
        </el-container>
        <v-navigation></v-navigation>
        <div>
          <el-button size="mini" @click="clearCache">clear cache</el-button>
        </div>
      </div>
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

  methods: {
    clearCache() {
      this.$http
          .delete('cache')
          .then(() => this.$notify.success('Cache cleared.'))
          .catch(() => this.$notify.error('Could not clear cache.'))
    }
  }
}
</script>
