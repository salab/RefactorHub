<script setup lang="ts">
import { Viewer } from '@/composables/useViewer'

defineProps({
  viewer: {
    type: Object as () => Viewer,
    required: true,
  },
})

const changeList = computed(() => useAnnotation().getChangeList())
const isDividingChange = computed(
  () =>
    !!useAnnotation().annotation.value?.hasTemporarySnapshot &&
    changeList.value[changeList.value.length - 2]?.id ===
      useAnnotation().currentChange.value?.id,
)
</script>

<template>
  <file-viewer v-if="viewer.type === 'file'" :viewer="viewer" />
  <diff3-viewer v-else-if="isDividingChange" :viewer="viewer" />
  <diff-viewer v-else :viewer="viewer" />
</template>
