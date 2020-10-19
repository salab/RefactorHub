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
import { defineComponent, computed, useContext } from '@nuxtjs/composition-api'
import { trimFileName } from '@/components/common/editor/utils/trim'

export default defineComponent({
  setup() {
    const {
      app: { $accessor },
    } = useContext()

    const displayedFile = computed(() => $accessor.draft.displayedFile)
    const files = computed(() => $accessor.draft.commit?.files)
    const fileName = computed(() => ({
      before: (() => {
        const index = displayedFile.value?.before?.index
        return files.value && index !== undefined
          ? files.value[index].previousName
          : ''
      })(),
      after: (() => {
        const index = displayedFile.value?.after?.index
        return files.value && index !== undefined ? files.value[index].name : ''
      })(),
    }))
    return {
      displayedFile,
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
