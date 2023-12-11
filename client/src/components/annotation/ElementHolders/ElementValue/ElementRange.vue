<script setup lang="ts">
import { Range } from '@/apis'

defineProps({
  range: {
    type: Object as () => Range | null,
    default: null,
  },
})
</script>

<template>
  <div class="d-flex align-center justify-center">
    <number-column :number="range ? range.startLine : undefined" />
    <div>:</div>
    <number-column :number="range ? range.startColumn : undefined" />
    <div class="px-1">~</div>
    <number-column :number="range ? range.endLine : undefined" />
    <div>:</div>
    <number-column :number="range ? range.endColumn : undefined" />
  </div>
</template>
