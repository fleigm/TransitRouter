<template>
  <div class="bg-white border rounded shadow-md">
    <template v-if="$slots.header || header">
      <div class="p-2 text-center text-secondary ">
        <slot name="header">
          {{ header }}
        </slot>
      </div>
    </template>
    <div class="p-2 relative">
      <slot></slot>
    </div>
    <div v-if="$slots.footer" class="p-2 border-t">
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<script>
export default {
  name: "EvaluationCard",

  props: {
    header: String
  }
}
</script>

