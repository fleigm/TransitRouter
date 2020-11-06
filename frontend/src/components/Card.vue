<template>
  <div class="bg-white border rounded shadow-md">
    <template v-if="$slots.header || header">
      <slot name="header">
        <div class="p-2 border-b">
          <h2>{{ header }}</h2>
        </div>
      </slot>
    </template>
    <slot></slot>
  </div>
</template>

<script>
export default {
  name: "v-card",

  props: ['header']
}
</script>