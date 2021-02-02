<script>
import _ from 'lodash';
import {bin} from "d3-array";
import VAccuracyChart from "./AccuracyChart";

export default {
  name: "v-reports",
  components: {VAccuracyChart},
  props: {
    feeds: {
      type: Array,
      required: true,
    }
  },

  render() {
    return this.$scopedSlots.default({
      reports: this.reports,
      accuracyDataSets: this.accuracyDataSets,
      avgFrechetDistanceDataSets: this.avgFrechetDistanceDataSets,
      anDataSets: this.anDataSets,
      alDataSets: this.alDataSets,
      chartOptions: this.chartOptions,
    });
  },

  data() {
    return {
      loading: [],
      cache: [],
      chartOptions: {
        legend: {
          display: false,
        }
      }
    }
  },

  computed: {
    reports() {
      return this.cache.filter(item => this.feeds.find(feed => feed.id === item.id));
    },

    accuracyDataSets() {
      return this.reports.map(report => {
        return {
          label: report.name,
          data: this.feeds.find(feed => feed.id === report.id).extensions['de.fleigm.ptmm.eval.EvaluationExtension'].quickStats.accuracy,
          backgroundColor: report.color,
        }
      })
    },

    avgFrechetDistanceDataSets() {
      const histogram = bin().thresholds([5, 10, 20, 40, 80, 320, 640, 1280, 2560, 10240]);

      return this.reports.map(report => {
        return {
          label: report.name,
          data: histogram(report.entries.map(e => e.avgFd)),
          backgroundColor: report.color,
        };
      });
    },

    anDataSets() {
      const histogram = bin().thresholds(20);

      return this.reports.map(report => {
        return {
          label: report.name,
          data: histogram(report.entries.map(e => e.an)),
          backgroundColor: report.color,
        };
      });
    },

    alDataSets() {
      const histogram = bin().thresholds(20);

      return this.reports.map(report => {
        return {
          label: report.name,
          data: histogram(report.entries.map(e => e.al)),
          backgroundColor: report.color,
        };
      });
    },
  },

  methods: {
    getFeeds() {
      this.feeds.forEach(this.fetch)
    },

    fetch(feed) {
      const idFilter = item => item.id === feed.id

      if (this.cache.find(idFilter) || this.loading.find(idFilter)) {
        return;
      }

      this.loading.push({id: feed.id});
      this.$http.get(`eval/${feed.id}/report?limit=999999`)
          .then(({data}) => {
            this.cache.push({id: feed.id, name: feed.name, entries: data.entries, color: feed._color})
          })
          .finally(() => {
            _.remove(this.loading, idFilter)
          });
    },
  },

  watch: {
    feeds: 'getFeeds'
  }
}
</script>

<style scoped>

</style>