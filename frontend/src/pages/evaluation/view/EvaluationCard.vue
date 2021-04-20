<template>
  <div class="bg-white border rounded shadow-md">
    <template v-if="$slots.header || header">
      <div class="p-2 relative flex">
        <div class="text-center text-secondary flex-grow">
          <slot name="header">
            {{ header }}
          </slot>
        </div>
<!--        <HelperTooltip content="Test Content" title="test"></HelperTooltip>-->
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

