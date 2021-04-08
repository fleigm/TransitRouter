<template>
  <div class="container">
    <div class="my-8 flex justify-between">
      <div></div>
      <div class="">
        <v-preset-upload-dialog></v-preset-upload-dialog>
      </div>
    </div>

    <div>
      <v-preset-card v-for="preset in presets" :key="preset.id" :preset="preset"></v-preset-card>
    </div>

    <div v-if="!isLoading && !presets.length">
      <div class="text-secondary text-2xl font-thin text-center py-8">There are currently no feeds.</div>
    </div>
  </div>
</template>

<script>

import PresetService from "../PresetService";
import VPresetCard from "./PresetCard";
import VPresetUploadDialog from "./PresetUploadDialog";

export default {
  name: "Overview",
  components: {VPresetUploadDialog, VPresetCard},
  data() {
    return {
      isLoading: false,
    }
  },

  computed: {
    presets() {
      return PresetService.state.presets;
    }
  },

  methods: {
  },

  mounted() {
    this.isLoading = true;
    PresetService
        .fetchPresets()
        .finally(() => this.isLoading = false);
  }
}
</script>