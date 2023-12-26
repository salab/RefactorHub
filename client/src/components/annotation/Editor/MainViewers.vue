<script setup lang="ts">
const { viewers } = useViewer()
const changeList = computed(() => useAnnotation().getChangeList())
const isDividingChange = computed(
  () =>
    !!useAnnotation().annotation.value?.hasTemporarySnapshot &&
    changeList.value[changeList.value.length - 2]?.id ===
      useAnnotation().currentChange.value?.id,
)
const windowCount = computed(() =>
  viewers.value.reduce((count, viewer) => {
    return (
      count + (viewer.type === 'diff' ? (isDividingChange.value ? 3 : 2) : 1)
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
        (viewer.type === 'diff' ? (isDividingChange ? 3 : 2) : 1)
      }%`"
    />
  </v-container>
</template>
