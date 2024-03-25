<script setup lang="ts">
const { viewers } = useViewer()
const windowCount = computed(() =>
  viewers.value.reduce((count, viewer) => {
    return (
      count +
      (viewer.type === 'diff'
        ? useAnnotation().isDividingChange.value
          ? 3
          : 2
        : 1)
    )
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
        (100 / windowCount) *
        (viewer.type === 'diff'
          ? useAnnotation().isDividingChange.value
            ? 3
            : 2
          : 1)
      }%`"
    />
  </v-container>
</template>
