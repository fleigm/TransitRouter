<template>
  <div>
    <v-card>
      <v-resource :endpoint="feedResource">
        <template slot-scope="{page, searchQuery, loading, getPage, search, sortBy}">
          <div>
            <el-table v-loading="loading"
                      :data="page.data"
                      size="mini"
                      stripe
                      :fit="true"
                      @sort-change="({prop, order}) => sortBy(prop, order)">

              <el-table-column label="route" width="100">
                <template slot-scope="scope">
                  <div>{{ scope.row.route.route_short_name }}</div>
                </template>
              </el-table-column>
              <el-table-column label="name">
                <template slot-scope="scope">
                  <div>{{ scope.row.route.route_long_name }}</div>
                </template>
              </el-table-column>
              <el-table-column label="type" width="150">
                <template slot-scope="scope">
                  <div>{{ scope.row.route.route_type | routeTypeToString }}</div>
                </template>
              </el-table-column>
              <el-table-column label="trips" width="80">
                <template slot-scope="scope">
                  <div>{{ scope.row.trips.length }}</div>
                </template>
              </el-table-column>
              <el-table-column label="stops" width="80">
                <template slot-scope="scope">
                  <div>{{ scope.row.stops.length }}</div>
                </template>
              </el-table-column>
              <el-table-column align="right" width="">
                <template slot="header" slot-scope="scope">
                  <div class="el-input el-input--mini">
                    <input type="text"
                           autocomplete="off"
                           placeholder="Type to search"
                           class="el-input__inner"
                           v-model="searchQuery"
                           @change="search(searchQuery)">
                  </div>
                </template>
                <template slot-scope="scope">
                  <el-button size="mini" @click="showDetails(scope.row)" type="text" circle
                             icon="el-icon-map-location"></el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="flex justify-center w-full py-2">
              <el-pagination
                  layout="prev, pager, next"
                  :total="page.total"
                  :page-size="page.perPage"
                  :current-page="page.currentPage"
                  @current-change="getPage">
              </el-pagination>
            </div>

            <el-dialog :visible.sync="showDetailsModal" width="80%" top="5vh">
              <template #title v-if="details">
                <span>{{ details.trip.trip_id }} - {{ details.route.route_short_name }}</span>
              </template>
              <div class="w-full" style="height: 80vh;" v-loading="loadingDetails">
                <v-routing-map :route="details" v-if="details"></v-routing-map>
              </div>
            </el-dialog>
          </div>
        </template>
      </v-resource>
    </v-card>
  </div>
</template>

<script>
export default {
  name: "v-preset-routing-index",

  props: {},

  data() {
    return {
      showDetailsModal: false,
      details: null,
      loadingDetails: false,
    }
  },

  computed: {
    id() {
      return this.$route.params.id
    },

    feedResource() {
      return `presets/${this.id}/feed`;
    }
  },

  methods: {
    showDetails(entry) {
      this.showDetailsModal = true;
      this.loadingDetails = true

      this.$http
          .get(`presets/${this.id}/feed/${(entry.trips[0].trip_id)}`)
          .then(({data}) => {
            this.loadingDetails = false;
            this.details = data;
          })
    },
  },

  mounted() {
  }
}
</script>