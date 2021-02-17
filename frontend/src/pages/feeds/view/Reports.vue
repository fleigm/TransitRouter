<script>
import {bin} from "d3-array";
import VAccuracyChart from "./AccuracyChart";

export default {
  name: "v-reports",
  components: {VAccuracyChart},
  props: {
    evaluations: {
      type: Array,
      required: true,
    },
    types: {
      type: Array,
      default: () => [0, 1, 2, 3],
    }
  },

  render() {
    return this.$scopedSlots.default({
      reports: this.reports,
      loading: this.loading,
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
      return this.cache.filter(item => this.evaluations.find(e => e.id === item.id));
    },

    accuracyDataSets() {
      return this.reports.map(report => {
        return {
          label: report.name,
          data: this.computeAccuracy(report),
          backgroundColor: report.color,
        }
      })
    },

    avgFrechetDistanceDataSets() {
      const histogram = bin().thresholds([5, 10, 20, 40, 80, 320, 640, 1280, 2560, 10240]);

      return this.reports.map(report => {
        return {
          label: report.name,
          data: histogram(report.entries.filter(this.typeFilter).map(e => e.avgFd)),
          backgroundColor: report.color,
        };
      });
    },

    anDataSets() {
      const histogram = bin().thresholds(20);

      return this.reports.map(report => {
        return {
          label: report.name,
          data: histogram(report.entries.filter(this.typeFilter).map(e => e.an)),
          backgroundColor: report.color,
        };
      });
    },

    alDataSets() {
      const histogram = bin().thresholds(20);

      return this.reports.map(report => {
        return {
          label: report.name,
          data: histogram(report.entries.filter(this.typeFilter).map(e => e.al)),
          backgroundColor: report.color,
        };
      });
    },
  },

  methods: {
    getReports() {
      this.evaluations.forEach(this.fetch)
    },

    fetch(evaluation) {
      const idFilter = item => item.id === evaluation.id

      let cachedReport = this.cache.find(idFilter)
      if (cachedReport) {
        cachedReport.color = evaluation.color;
        return;
      }

      if (this.loading.includes(evaluation.id)) {
        return;
      }

      this.loading.push(evaluation.id);
      this.$http.get(`feeds/${evaluation.id}/report`)
          .then(({data}) => {
            this.cache.push({
              id: evaluation.id,
              name: evaluation.feed.name,
              entries: data.entries,
              color: evaluation.color
            })
          })
          .finally(() => {
            const index = this.loading.indexOf(x => x.id === evaluation.id);
            this.loading.splice(index, 1);
          });
    },

    typeFilter(entry) {
      return this.types.includes(entry.type);
    },

    computeAccuracy(report) {
      const accuracy = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0]

      const entries = report.entries.filter(this.typeFilter);

      for (let entry of entries) {
        for (let i = 0; i < 10; i++) {
          if (entry.an <= i * 0.1) {
            accuracy[i]++
          }
        }
      }

      for (let i = 0; i < 10; i++) {
        accuracy[i] /= entries.length;
      }

      return accuracy;
    }
  },

  watch: {
    evaluations: 'getReports'
  }
}
</script>

<style scoped>

</style>