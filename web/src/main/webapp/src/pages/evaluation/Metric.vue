<template>
  <div class="p-2 flex flex-col items-center">
    <div>
      <span class="text-4xl font-thin">{{ value }}</span>
      <span class="text-sm">{{ unit }}</span>
    </div>
    <div class="text-secondary font-thin">{{ title }}</div>
  </div>
</template>

<script>
export default {
  name: "v-metric",

  props: {
    value: String | Object,
    unit: String,
    title: String,

  }
}
</script>