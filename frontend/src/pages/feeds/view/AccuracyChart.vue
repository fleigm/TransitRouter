<script>
import {Bar} from 'vue-chartjs';
import {defaultsDeep} from "lodash";

const defaultOptions = {
  responsive: true,
  maintainAspectRatio: false,
  scales: {
    xAxes: [{
      ticks: {
        autoSkip: true,
      }
    }]
  }
};

const defaultDataSetOptions = {
  backgroundColor: '#075985',
  minBarLength: 2,
};

export default {
  name: "v-accuracy-chart",

  extends: Bar,

  props: {
    dataSets: {
      type: Array,
      required: true,
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
    render() {
      this.renderChart({
        labels: ['0', '10', '20', '30', '40', '50', '60', '70', '80', '90'],
        datasets: this.dataSets
      }, this.chartOptions)
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