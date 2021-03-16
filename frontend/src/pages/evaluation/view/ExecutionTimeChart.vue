<template>
  <div class="relative" v-if="extension">
    <v-doughnut-chart :data="dataSet" :options="options" class="relative h-48"></v-doughnut-chart>
    <div class="absolute w-full bottom-1/4 text-center text-xl text-secondary">
      total: {{ total | number('0.0') }}m
    </div>
  </div>
  <div v-else class="text-secondary p-2">
    Missing execution time data...
  </div>
</template>

<script>

import moment from "moment";
import number from "../../../filters/NumberFilter";

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
      render: (args) => number(args.value, '0.0') + 'm',
      fontColor: '#fff',
      precision: 0,
    }
  }
};


export default {
  name: "ExecutionTimeChart",

  props: {
    feed: {
      type: Object,
      required: true,
    },
  },

  computed: {
    options() {
      return defaultOptions;
    },

    dataSet() {
      let labels = [];
      let data = [];

      this.times.forEach(([key, duration]) => {
        labels.push(key);
        data.push(duration);
      });

      labels.push('rest');
      data.push(this.rest);

      return {
        labels,
        datasets: [{
          data,
          backgroundColor: ['#1e3a8a', '#1e40af', '#1d4ed8', '#2563eb', '#3b82f6', '#60a5fa',]
        }]
      }
    },

    extension() {
      return this.feed.extensions['de.fleigm.transitrouter.feeds.process.ExecutionTime'];
    },

    times() {
      let durations = this.extension.durations;

      return Object.entries(durations)
          .filter(([key]) => key !== 'total')
          .map(([key, duration]) => [key, moment.duration(duration, 's').asMinutes()])
    }
    ,

    total() {
      return moment.duration(this.extension.durations.total, 's').asMinutes();
    },

    rest() {
      let rest = this.total;
      this.times.forEach(([key, duration]) => {
        rest = rest - duration
      });
      return rest;
    },
  },

  methods: {},
}
</script>
