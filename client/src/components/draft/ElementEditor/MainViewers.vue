<script setup lang="ts">
const viewers = useViewer().viewers.value
const windowCount = computed(() =>
  viewers.value.reduce((count, viewer) => {
    return count + (viewer.type === 'diff' ? 2 : 1)
  }, 0),
)
</script>

<template>
  <v-container fluid class="fill-height d-flex flex-row flex-nowrap pa-0">
    <main-viewer
      v-for="viewer in viewers"
      :key="viewer.id"
      :viewer="viewer"
      class="flex-grow-1 flex-shrink-1"
      :style="`min-width: ${
        (100 / windowCount) * (viewer.type === 'diff' ? 2 : 1)
      }%`"
    />
  </v-container>
</template>
