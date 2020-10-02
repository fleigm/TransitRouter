<template>
  <div>
    <l-animated-polyline v-if="options.showShape"
                         :lat-lngs="routingResult.shape"
                         :options="{'delay': 2400}">
      <l-popup>
        <div>Distance: {{ routingResult.distance }}</div>
        <div>Time: {{ routingResult.time }}</div>
      </l-popup>
    </l-animated-polyline>

    <l-animated-polyline v-if="options.showOriginalShape"
                         :lat-lngs="routingResult.originalShape"
                         :options="{'delay': 2400, 'color': '#000'}"
    ></l-animated-polyline>

    <template v-for="timeStep in routingResult.timeSteps">
      <l-circle v-for="candidate in timeStep.candidates"
                :key="candidate.id"
                :radius="1"
                :lat-lng="candidate.state.position">
        <l-popup>
          <pre>{{ candidate }}</pre>
        </l-popup>
      </l-circle>
    </template>

    <l-circle v-if="options.showStops"
              v-for="stop in routingResult.stops"
              :key="stop.id"
              color="red"
              :radius="2"
              :lat-lng="[stop.stop_lat, stop.stop_lon]">
      <l-popup>
        <pre>{{ stop }}</pre>
      </l-popup>
    </l-circle>
  </div>
</template>

<script>
export default {
  props: ['routingResult', 'options']
}
</script>