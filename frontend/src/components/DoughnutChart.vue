<script>
import {Doughnut} from 'vue-chartjs';

export default {
  name: "v-doughnut-chart",

  extends: Doughnut,

  props: {
    data: {
      type: Object,
      default: null
    },
    options: {
      type: Object,
      default: null
    }
  },

  mounted() {
    this.renderChart(this.data, this.options)
  }
}
</script>
