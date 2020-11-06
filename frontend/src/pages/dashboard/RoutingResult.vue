<template>
  <div>
    <l-animated-polyline v-if="options.showShape"
                         :lat-lngs="routingResult.generatedShape"
                         :options="{'delay': 2400, fillOpacity: 0.5, opacity: 0.5}">
      <l-popup>
        <div>Distance: {{ routingResult.distance }}</div>
        <div>Time: {{ routingResult.time }}</div>
      </l-popup>
    </l-animated-polyline>

    <l-animated-polyline v-if="options.showOriginalShape"
                         :lat-lngs="routingResult.originalShape"
                         :options="{'delay': 2400, 'color': '#000', fillOpacity: 0.5, opacity: 0.5}"
    ></l-animated-polyline>

    <l-circle v-for="(candidate, i) in routingResult.candidates"
              :key="i"
              :radius="1"
              :lat-lng="candidate">
    </l-circle>

    <l-circle v-if="options.showStops"
              v-for="(stop, i) in routingResult.stops"
              :key="i"
              color="red"
              :radius="1"
              :lat-lng="[stop.stop_lat, stop.stop_lon]">
      <l-popup>
        <pre>{{ stop }}</pre>
      </l-popup>
    </l-circle>
  </div>
</template>

<script>
const defaultOptions = {
  showOriginalShape: true,
  showShape: true,
  showStops: true,
}

export default {
  name: 'v-routing-result',
  props: {
    routingResult: {
      type: Object,
      required: true,
    },
    options: {
      type: Object,
      default: () => defaultOptions
    }
  }
}
</script>