<template>
  <el-breadcrumb separator-class="el-icon-arrow-right" class="my-8">
    <div></div>
    <el-breadcrumb-item v-for="(item, i) in items"
                        :key="i"
                        :to="item.route">
      {{ item.name }}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script>
import Breadcrumbs from "./index";

export default {
  name: "v-breadcrumbs",

  computed: {
    items() {
      return Breadcrumbs.items;
    }
  },
}
</script>