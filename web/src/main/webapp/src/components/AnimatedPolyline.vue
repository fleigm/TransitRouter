<template>
  <div style="display: none;">
    <slot v-if="ready" />
  </div>
</template>

<script>
import { optionsMerger, propsBinder, findRealParent } from 'vue2-leaflet/src/utils/utils';
import PolylineMixin from 'vue2-leaflet/src/mixins/Polyline.js';
import Options from 'vue2-leaflet/src/mixins/Options.js';
import { DomEvent } from 'leaflet';
import { AntPath } from 'leaflet-ant-path';

/**
 * Easily draw a polyline on the map
 */
export default {
  name: 'l-animated-polyline',
  mixins: [PolylineMixin, Options],
  props: {
    latLngs: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      ready: false,
    };
  },
  mounted() {
    const options = optionsMerger(this.polyLineOptions, this);
    this.mapObject = new AntPath(this.latLngs, options);
    DomEvent.on(this.mapObject, this.$listeners);
    propsBinder(this, this.mapObject, this.$options.props);
    this.ready = true;
    this.parentContainer = findRealParent(this.$parent);
    this.parentContainer.addLayer(this, !this.visible);
    this.$nextTick(() => {
      /**
       * Triggers when the component is ready
       * @type {object}
       * @property {object} mapObject - reference to leaflet map object
       */
      this.$emit('ready', this.mapObject);
    });
  },
};
</script>