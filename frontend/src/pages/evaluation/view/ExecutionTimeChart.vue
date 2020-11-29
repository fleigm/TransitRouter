<template>
  <div class="relative">
    <div class="text-center text-secondary mb-4">execution times in s</div>
    <div class="relative">
      <v-doughnut-chart :data="data" :options="options" class="relative h-48"></v-doughnut-chart>
      <div class="absolute w-full bottom-1/4 text-center text-xl">
        total: 324s
      </div>
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

  data() {
    return {
      data: {
        labels: ['shape generation', 'evaluation', 'rest'],
        datasets: [{
          data: [158, 147, 324 - 158 - 147],
          backgroundColor: ['#075985', '#0369A1', '#0284C7']
        }]
      }
    }
  },

  computed: {
    options() {
      return defaultOptions;
    }
  },

  methods: {},
}
</script>
