<template>
  <div class="p-2 flex flex-col items-center">
    <div>
      <span :class="[valueSize, 'font-thin']"><slot>{{ value }}</slot></span>
      <span class="text-sm">{{ unit }}</span>
    </div>
    <div class="text-secondary font-thin">{{ title }}</div>
  </div>
</template>

<script>
export default {
  name: "v-metric",

  props: {
    value: String | Number | Object,
    unit: String,
    title: String,
    size: {
      type: String,
      default: () => {
        return 'normal'
      },
      validator: function (value) {
        return ['mini', 'small', 'normal'].indexOf(value) !== -1
      }
    }
  },

  computed: {
    valueSize() {
      if (this.size === 'mini') {
        return 'text-xl'
      } else if (this.size === 'small') {
        return 'text-2xl'
      } else {
        return 'text-4xl'
      }
    }
  }
}
</script>