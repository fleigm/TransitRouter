<template>
  <v-resource :endpoint="endpoint" :page-size="15">
    <div
        slot-scope="{page, searchQuery, loading, getPage, search, sortBy}">
      <el-table :data="page.data"
                size="small"
                stripe
                @sort-change="({prop, order}) => sortBy(prop, order)">
        <el-table-column prop="tripId"
                         label="Trip"
                         width="300"
        ></el-table-column>
        <el-table-column prop="an"
                         label="A_n"
                         width="100"
                         sortable="custom"
        ></el-table-column>
        <el-table-column prop="al"
                         label="A_l"
                         width="100"
                         sortable="custom"
        ></el-table-column>
        <el-table-column prop="avgFd"
                         label="avg FD"
                         widht="100"
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
  }
}
</script>