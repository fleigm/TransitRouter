<template>
  <v-resource :endpoint="endpoint" :page-size="10">
    <div
        slot-scope="{page, searchQuery, loading, getPage, search, sortBy}">
      <el-table v-loading="loading"
                :data="page.data"
                size="small"
                stripe
                :fit="true"
                @sort-change="({prop, order}) => sortBy(prop, order)">
        <el-table-column prop="tripId"
                         label="Trip"
        ></el-table-column>
        <el-table-column prop="route"
                         label="Route"
        ></el-table-column>
        <el-table-column prop="an"
                         label="A_n"
                         sortable="custom"
        ></el-table-column>
        <el-table-column prop="al"
                         label="A_l"
                         sortable="custom"
        ></el-table-column>
        <el-table-column prop="avgFd"
                         label="avg FD"
                         sortable="custom"
        ></el-table-column>
        <el-table-column
            align="right">
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
            <el-button size="mini" @click="showDetails(scope.row)" type="text" circle icon="el-icon-map-location"></el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="flex justify-center w-full py-2">
        <el-pagination
            background
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
  </v-resource>
</template>

<script>
export default {
  name: "v-report-list",

  props: {
    name: String
  },

  computed: {
    endpoint() {
      return `eval/${this.name}/trips`
    }
  },

  data() {
    return {
      showDetailsModal: false,
      details: null,
      loadingDetails: false,
    }
  },

  methods: {
    showDetails(entry) {
      this.showDetailsModal = true;
      this.loadingDetails = true

      const name = this.$route.params.name;
      const tripId = entry.tripId;

      this.$http
          .get(`eval/${name}/trips/${tripId}`)
          .then(({data}) => {
            this.loadingDetails = false;
            this.details = data;
          })
    }
  }
}
</script>