<template>
  <div class="relative">
    <v-doughnut-chart :data="data" :options="options" class="relative h-48"></v-doughnut-chart>
    <div class="absolute w-full bottom-1/4 text-center text-xl text-secondary">
      total: {{ timeTotal }}s
    </div>
  </div>
</template>

<script>

const defaultOptions = {
  rotation: Math.PI,
  circumference: Math.PI,
  cutoutPercentage: 50,
  legend: {
    position: 'bottom',
  },
  responsive: true,
  maintainAspectRatio: false,

  plugins: {
    labels: {
      render: (args) => args.value + 's',
      fontColor: '#fff'
    }
  }
};


export default {
  name: "ExecutionTimeChart",

  props: {
    info: {
      type: Object,
      required: true,
    },
  },

  computed: {
    options() {
      return defaultOptions;
    },

    data() {
      return {
        labels: ['shape generation', 'evaluation', 'rest'],
        datasets: [{
          data: [
            this.timeShapeGeneration,
            this.timeEvaluation,
            this.timeRest,
          ],
          backgroundColor: ['#075985', '#0369A1', '#0284C7']
        }]
      }
    },

    timeShapeGeneration() {
      return Math.round(this.info.statistics['executionTime.shapeGeneration'] / 1000);
    },
    timeEvaluation() {
      return Math.round(this.info.statistics['executionTime.evaluation'] / 1000);
    },
    timeTotal() {
      return Math.round(this.info.statistics['executionTime.total'] / 1000);
    },
    timeRest() {
      return this.timeTotal - this.timeShapeGeneration - this.timeEvaluation;
    }
  },

  methods: {},
}
</script>
