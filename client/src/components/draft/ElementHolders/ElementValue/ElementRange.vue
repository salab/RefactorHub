<template>
  <div class="d-flex align-center justify-center text--secondary">
    <number-column :number="range ? range.startLine : undefined" />
    <div>:</div>
    <number-column :number="range ? range.startColumn : undefined" />
    <div class="px-1">~</div>
    <number-column :number="range ? range.endLine : undefined" />
    <div>:</div>
    <number-column :number="range ? range.endColumn : undefined" />
  </div>
</template>

<script lang="ts">
import { defineComponent } from '@nuxtjs/composition-api'
import { Range } from '@/apis'

export default defineComponent({
  props: {
    range: {
      type: Object as () => Range,
    },
  },
})
</script>
