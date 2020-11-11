<template>
  <div>
    <v-promise :promise="fetchRoutes">
      <template #default="{ data: routes }">
        <div class="">
          <div v-for="route in routes"
               :key="route.id"
               class="flex items-center py-2 px-2 border-b cursor-pointer hover:bg-secondary-inverse"
               @click="selectedRoute(route.route_id)">
            <div class="mr-2">{{ route.route_short_name }}</div>
            <div class="text-secondary text-xs">{{ route.route_long_name }}</div>
          </div>
        </div>
      </template>
      <template #pending>
        <v-spinner></v-spinner>
      </template>
    </v-promise>
  </div>
</template>

<script>
import gtfsApi from '../../gtfs/GtfsApi';

export default {
  name: "v-route-list",

  props: {

  },

  data() {
    return {}
  },

  computed: {
    fetchRoutes() {
      return gtfsApi.fetchRoutes();
    }
  },
  
  methods: {
    selectedRoute(id) {
        this.$emit('select', id);
    }
  },

  created() {
  }
}
</script>
