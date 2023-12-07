<script setup lang="ts">
const selectedToolName = ref('')
const toolNames = {
  commitInfo: 'commitInfo',
  compare: 'compare',
  highlightSetting: 'highlightSetting',
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
        prepend-icon="$mdiInformation"
        text="Commit Information"
        stacked
        size="x-small"
        :value="toolNames.commitInfo"
      />
      <v-btn
        prepend-icon="$mdiCompare"
        text="Compare Elements"
        stacked
        size="x-small"
        :value="toolNames.compare"
      />
      <v-btn
        prepend-icon="$mdiMarker"
        text="Highlight Setting"
        stacked
        size="x-small"
        :value="toolNames.highlightSetting"
      />
    </v-btn-toggle>
  </v-sheet>
  <v-divider />
  <v-window v-model="selectedToolName">
    <v-window-item :value="toolNames.commitInfo">
      <commit-info-tool />
    </v-window-item>
    <v-window-item :value="toolNames.compare">
      <compare-tool />
    </v-window-item>
    <v-window-item :value="toolNames.highlightSetting">
      <highlight-setting-tool />
    </v-window-item>
  </v-window>
</template>
