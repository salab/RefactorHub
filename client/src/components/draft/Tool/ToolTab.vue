<script setup lang="ts">
const selectedToolName = ref('')
const toolNames = {
  compare: 'compare',
}
onMounted(() => (selectedToolName.value = ''))
</script>

<template>
  <v-sheet color="primary">
    <v-btn-toggle
      v-model="selectedToolName"
      variant="text"
      divided
      density="compact"
      :rounded="0"
    >
      <v-btn
        prepend-icon="$mdiCompare"
        text="Compare Elements"
        stacked
        size="x-small"
        :value="toolNames.compare"
      />
    </v-btn-toggle>
  </v-sheet>
  <v-divider />
  <v-window v-model="selectedToolName">
    <v-window-item :value="toolNames.compare">
      <compare-tool />
    </v-window-item>
  </v-window>
</template>
