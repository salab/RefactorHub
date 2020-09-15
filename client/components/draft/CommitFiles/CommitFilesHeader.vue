<template>
  <div class="files-header">
    <div class="content d-flex align-center pl-2">
      <span
        v-if="fileName.before"
        :title="fileName.before"
        class="subtitle-1"
        >{{ trimFileName(fileName.before) }}</span
      >
    </div>
    <v-divider vertical />
    <div class="content d-flex align-center pl-2">
      <span v-if="fileName.after" :title="fileName.after" class="subtitle-1">{{
        trimFileName(fileName.after)
      }}</span>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, computed } from '@nuxtjs/composition-api'
import { trimFileName } from '@/components/common/editor/use/trim'

export default defineComponent({
  name: 'CommitFilesHeader',
  setup(_, { root }) {
    const commitFiles = computed(() => root.$accessor.draft.commitInfo?.files)
    const displayedFileMetadata = computed(
      () => root.$accessor.draft.displayedFileMetadata
    )
    const fileName = computed(() => ({
      before: (() => {
        const index = displayedFileMetadata.value?.before?.index
        return commitFiles.value && index !== undefined
          ? commitFiles.value[index].previousName
          : ''
      })(),
      after: (() => {
        const index = displayedFileMetadata.value?.after?.index
        return commitFiles.value && index !== undefined
          ? commitFiles.value[index].name
          : ''
      })(),
    }))
    return {
      commitFiles,
      displayedFileMetadata,
      fileName,
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
