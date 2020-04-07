<template>
  <div v-if="active" class="background d-flex align-center justify-center">
    <v-progress-circular indeterminate />
  </div>
</template>

<script lang="ts">
import { defineComponent } from '@vue/composition-api'
export default defineComponent({
  name: 'loading',
  props: {
    active: {
      type: Boolean,
      default: false,
    },
  },
})
</script>

<style lang="scss" scoped>
.background {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  background: white;
  opacity: 0.5;
  z-index: 9999;
}
</style>
