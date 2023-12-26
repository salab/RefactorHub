<script setup lang="ts">
import { Viewer } from '@/composables/useViewer'

defineProps({
  viewer: {
    type: Object as () => Viewer,
    required: true,
  },
})
</script>

<template>
  <file-viewer v-if="viewer.type === 'file'" :viewer="viewer" />
  <diff-viewer v-else :viewer="viewer" />
</template>
