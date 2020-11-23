<template>
  <div class="flex">
    <router-link v-for="item in items"
                 :key="item.page"
                 :to="{name: item.page}"
                 class="text-lg font-thin py-4 px-2 border-b-2 border-transparent hover:border-blue-500">
      {{ item.label }}
    </router-link>
  </div>
</template>

<script>
export default {
  name: "v-navigation",

  data() {
    return {
      items: [
        {
          label: 'Dashboard',
          page: 'dashboard.index'
        }, {
          label: 'Evaluation',
          page: 'evaluation.index'
        }, {
          label: 'Routing',
          page: 'routing.index'
        }, {
          label: 'Candidate Finder',
          page: 'candidateFinder.index'
        }
      ]
    }
  }
}
</script>
