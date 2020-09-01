<template>
  <el-menu router :default-active="$route.path">
    <template v-for="(rule, index) in $router.options.routes">
      <el-submenu v-if="rule.children && rule.children.length > 0"
                  :key="index"
                  :index="rule.path"
      >
        <template slot="title"><i :class="rule.icon"></i>{{ rule.name }}</template>
        <el-menu-item v-for="(child, index) in rule.children"
                      :key="index"
                      :index="rule.path + '/' + child.path"
        >{{ child.name }}
        </el-menu-item>
      </el-submenu>
      <el-menu-item v-else
                    :key="index"
                    :index="rule.path"
      ><i :class="rule.icon"></i>
        {{ rule.name }}
      </el-menu-item>
    </template>
  </el-menu>
</template>

<script>
export default {
  name: "v-navigation"
}
</script>
