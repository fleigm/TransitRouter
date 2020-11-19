<template>
  <el-form :inline="true">
    <el-form-item label="Candidate Search Radius">
      <el-input-number size="mini" v-model="options.candidateSearchRadius" :min="0" controls-position="right"></el-input-number>
    </el-form-item>
    <el-form-item label="sigma">
      <el-input-number size="mini" v-model="options.sigma" :min="0" controls-position="right"></el-input-number>
    </el-form-item>
    <el-form-item label="beta">
      <el-input-number size="mini" v-model="options.beta" :precision="3" :step="0.05" :min="0" controls-position="right"></el-input-number>
    </el-form-item>
    <el-form-item label="u-turn distance penalty">
      <el-input-number size="mini" v-model="options.uTurnDistancePenalty" :min="0" controls-position="right"></el-input-number>
    </el-form-item>
  </el-form>
</template>

<script>
import TransitRouter from "./TransitRouter";

export default {
  data() {
    return {
      options: TransitRouter.options
    }
  }
}
</script>