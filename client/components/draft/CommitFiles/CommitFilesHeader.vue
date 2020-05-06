<template>
  <div class="files-header">
    <div class="content d-flex align-center pl-2">
      <span
        v-if="commitFiles && displayedFileMetadata.before.index !== undefined"
        :title="commitFiles[displayedFileMetadata.before.index].previousName"
        class="subtitle-1"
        >{{
          trimFileName(
            commitFiles[displayedFileMetadata.before.index].previousName
          )
        }}</span
      >
    </div>
    <v-divider vertical />
    <div class="content d-flex align-center pl-2">
      <span
        v-if="commitFiles && displayedFileMetadata.after.index !== undefined"
        :title="commitFiles[displayedFileMetadata.after.index].name"
        class="subtitle-1"
        >{{
          trimFileName(commitFiles[displayedFileMetadata.after.index].name)
        }}</span
      >
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, computed } from '@vue/composition-api'
import { trimFileName } from '@/components/common/editor/use/trim'

export default defineComponent({
  name: 'CommitFilesHeader',
  setup(_, { root }) {
    const commitFiles = computed(() => root.$accessor.draft.commitInfo?.files)
    const displayedFileMetadata = computed(
      () => root.$accessor.draft.displayedFileMetadata
    )
    return {
      commitFiles,
      displayedFileMetadata,
      trimFileName,
    }
  },
})
</script>

<style lang="scss" scoped>
.files-header {
  display: flex;
  position: absolute;
  width: 100%;
  height: 100%;

  .content {
    width: 50%;
    overflow: hidden;
  }
}
</style>
