<script setup lang="ts">
import { Refactoring } from '@/apis'

defineProps({
  refactorings: {
    type: Array as () => Refactoring[],
    required: true,
  },
})
</script>

<template>
  <div>
    <refactoring-item
      v-for="refactoring in refactorings"
      :key="refactoring.id"
      :refactoring="refactoring"
      class="my-4"
    />
  </div>
</template>
