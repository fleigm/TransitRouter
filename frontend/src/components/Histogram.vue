<script>
import {Bar} from 'vue-chartjs';
import {defaults, defaultsDeep} from 'lodash';

const defaultOptions = {
  responsive: true,
  maintainAspectRatio: false,
  scales: {
    xAxes: [{
      ticks: {
        autoSkip: true,
      }
    }],
    yAxes: [{
      ticks: {
        beginAtZero: true,
      }
    }]
  }
};

const defaultDataSetOptions = {
  backgroundColor: '#075985',
  minBarLength: 2,
};

export default {
  name: "v-histogram",

  extends: Bar,

  props: {
    dataSets: {
      type: [Array, Object],
      required: true,
    },
    convertFromD3js: {
      type: Boolean,
      default: true,
    },
    options: Object,
    xLabel: String,
    yLabel: String
  },

  computed: {
    chartOptions() {
      const labelOptions = {
        scales: {
          xAxes: [{
            scaleLabel: {
              display: !!this.xLabel,
              labelString: this.xLabel,
            }
          }],
          yAxes: [{
            scaleLabel: {
              display: !!this.yLabel,
              labelString: this.yLabel,
            }
          }],
        }
      }
      return defaultsDeep(this.options, defaultOptions, labelOptions)
    },
  },

  methods: {
    getLabels() {
      if (Array.isArray(this.dataSets)) {
        return this.dataSets[0].data.map(bin => bin.x1);
      }

      return this.dataSets.data.map(bin => bin.x1);
    },

    buildDataSets() {
      if (Array.isArray(this.dataSets)) {
        return this.dataSets.map(this.buildDataSet);
      }
      return [this.buildDataSet(this.dataSets)];
    },

    buildDataSet(dataSet) {
      const d = defaults(dataSet, defaultDataSetOptions);
      d.data = dataSet.data.map(bin => bin.length);

      return d;
    },

    render() {
      this.renderChart({
        labels: this.getLabels(),
        datasets: this.buildDataSets(),
      }, this.chartOptions);
    }
  },

  watch: {
    dataSets: 'render',
    options: 'render',
  },

  mounted() {
    this.render();
  }
}
</script>

<style scoped>

</style>