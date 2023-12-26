<script setup lang="ts">
import { CollapsedDirectory } from 'utils/path'

defineProps({
  isOpeningFileList: {
    type: Boolean,
    required: true,
  },
  viewerId: {
    type: String,
    required: true,
  },
  onFileChange: {
    type: Function,
    required: true,
  },
})

const collapsedDirectory = computed(() =>
  getFileTreeStructure(
    useAnnotation()
      .getCurrentFilePairs()
      .map((filePair) => {
        if (filePair.status === 'not found') return filePair.notFoundPath
        if (filePair.status === 'added') return filePair.after.path
        return filePair.before.path
      }),
    '(Project Root)',
  ),
)
function getDirectories(collapsedDirectory: CollapsedDirectory) {
  const directories = [collapsedDirectory.collapsedName]
  collapsedDirectory.directories.forEach((directory) => {
    directories.push(...getDirectories(directory))
  })
  return directories
}
const allDirectories = computed(() => getDirectories(collapsedDirectory.value))
</script>

<template>
  <v-sheet v-if="isOpeningFileList" style="width: 100%; height: 100%">
    <h2 class="mx-1">Changed File List</h2>
    <div style="width: 100%; height: 100%; overflow-y: scroll">
      <v-list
        :opened="allDirectories.map((d) => `${viewerId}:${d}`)"
        class="py-0 mx-1"
        ><file-list
          :viewer-id="viewerId"
          :file-tree="collapsedDirectory"
          former-path=""
          :on-file-change="() => onFileChange()"
        />
      </v-list>
    </div>
  </v-sheet>
</template>
