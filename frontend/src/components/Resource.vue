<script>
import _ from 'lodash';

export default {
  name: 'v-resource',

  props: {
    endpoint: {
      type: String,
      required: true
    },
    pageSize: {
      type: Number,
      default: 10
    }
  },

  data() {
    return {
      page: {},
      searchQuery: '',
      sortQuery: '',
      loading: false,
    }
  },

  computed: {
    hasPreviousPage() {
      return this.page.currentPage && this.page.currentPage !== 1;
    },

    hasNextPage() {
      return this.page.currentPage && this.page.currentPage !== this.page.lastPage && this.page.lastPage !== 0;
    },

    urlSearchQueryName() {
      return _.escapeRegExp(this.endpoint) + '-search';
    },

    urlSortQueryName() {
      return _.escapeRegExp(this.endpoint) + '-sort';
    },

    hasResults() {
      return this.page.data && this.page.data.length > 0;
    }
  },

  methods: {
    fetch(page = 1) {
      if (page <= 0 || page > this.page.lastPage) return;

      this.loading = true;

      return this.$http
          .get(`${this.endpoint}?page=${page}&limit=${this.pageSize}&search=${this.searchQuery}&sort=${this.sortQuery}`)
          .then(response => {
            this.page = response.data;
            this.loading = false;
            return response;
          })
          .finally(() => {
            this.loading = false;
          });
    },

    search(query) {
      this.searchQuery = query;

      /*const urlQuery = {};
      urlQuery[this.urlSearchQueryName] = this.searchQuery;
      urlQuery[this.urlSortQueryName] = this.sortQuery;

      this.$router.push({query: urlQuery});*/

      return this.fetch();
    },

    sortBy(attribute, order) {
      const allowedOrders = ['asc', 'ascending', 'desc', 'descending'];
      order = order.toLowerCase();

      if (!allowedOrders.includes(order)) {
        throw `Invalid value ${order} for order.`;
      }

      if (order === 'ascending') {
        order = 'asc';
      } else if (order === 'descending') {
        order = 'desc';
      }

      this.sortQuery = attribute + ":" + order

      this.fetch()
    }
  },

  render() {
    return this.$slots.default({
      page: this.page,
      loading: this.loading,
      searchQuery: this.searchQuery,
      sortQuery: this.sortQuery,
      hasResults: this.hasResults,
      hasPreviousPage: this.hasPreviousPage,
      hasNextPage: this.hasNextPage,
      getNextPage: () => this.fetch(this.page.currentPage + 1),
      getPreviousPage: () => this.fetch(this.page.currentPage - 1),
      getPage: (page) => this.fetch(page),
      search: this.search,
      sortBy: this.sortBy,
      fetch: this.fetch,
    });
  },

  created() {
    this.searchQuery = this.$route.query[this.urlSearchQueryName] || '';
    this.sortQuery = this.$route.query[this.urlSortQueryName] || '';
    this.fetch();
  }
}
</script>