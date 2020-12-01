<template>
  <v-histogram :data-sets="data" :options="options"></v-histogram>
</template>

<script>
import {bin} from "d3-array";

export default {
  name: "HistogramAverageFrechetDistance",

  props: {
    report: {
      type: Array,
      required: true,
    }
  },

  data() {
    return {
      options: {
        legend: {
          display: false,
        }
      }
    };
  },

  computed: {
    data() {
      const histogram = bin().thresholds([5, 10, 20, 40, 80, 320, 640, 1280, 2560, 10240]);

      console.log(histogram(this.report.map(e => e.avgFd)));

      return {
        label: 'vehicle count',
        data: histogram(this.report.map(e => e.avgFd)),
      };
    },
  }
}
</script>