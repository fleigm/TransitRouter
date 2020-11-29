<template>
  <v-histogram :data-sets="data" :options="options"></v-histogram>
</template>

<script>
import {bin} from "d3-array";

export default {
  name: "HistogramMismatchedHopSegments",

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
      const histogram = bin().thresholds(20);
      return {
        label: 'vehicle count',
        data: histogram(this.report.map(e => e.an)),
      };
    },
  }
}
</script>