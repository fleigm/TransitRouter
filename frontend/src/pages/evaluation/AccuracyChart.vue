<script>
import {Bar} from 'vue-chartjs';

export default {
  name: "v-accuracy-chart",

  extends: Bar,

  props: {
    accuracies: Array
  },

  mounted() {
    this.renderChart({
      labels: ['0', '10', '20', '30', '40', '50', '60', '70', '80', '90'],
      datasets: [{
        label: 'Accuracy',
        data: this.accuracies,
        backgroundColor: '#075985',
      }]
    }, {
      responsive: true,
      maintainAspectRatio: false,
    })
  }
}
</script>