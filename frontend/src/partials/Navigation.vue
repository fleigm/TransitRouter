<template>
  <el-menu router :default-active="$route.path" mode="horizontal">
    <template v-for="(route, index) in navigation">
      <el-submenu v-if="route.children && route.children.length > 0"
                  :key="index"
                  :index="route.page"
      >
        <template slot="title"><i :class="route.icon"></i>{{ route.name }}</template>
        <el-menu-item v-for="(child, index) in route.children"
                      :key="index"
                      :index="route.page"
        >{{ child.name }}
        </el-menu-item>
      </el-submenu>
      <el-menu-item v-else
                    :key="index"
                    :index="route.page"
                    :route="{name: route.page}"
      ><i :class="route.icon"></i>
        {{ route.name }}
      </el-menu-item>
    </template>
  </el-menu>
</template>

<script>
export default {
  name: "v-navigation",

  data() {
    return {
      navigation: [
        {
          name: 'Dashboard',
          page: 'dashboard.index'
        }, {
          name: 'Evaluation',
          page: 'evaluation.index'
        }, {
          name: 'Routing',
          page: 'routing.index'
        }, {
          name: 'Candidate Finder',
          page: 'candidateFinder.index'
        }, {
          name: 'Wiki',
          page: 'wiki.index',
        }
      ]
    }
  }
}
</script>
