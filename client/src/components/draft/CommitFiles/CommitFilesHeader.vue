<script setup lang="ts">
import { CommitFile } from '@/apis'
import { trimFileName } from '@/components/common/editor/utils/trim'

const props = defineProps({
  files: {
    type: Array as () => CommitFile[],
    required: true,
  },
})

const displayedFile = computed(() => useDraft().displayedFile.value)
const fileName = computed(() => ({
  before: (() => {
    const index = displayedFile.value?.before?.index
    return index !== undefined ? props.files[index].previousName : ''
  })(),
  after: (() => {
    const index = displayedFile.value?.after?.index
    return index !== undefined ? props.files[index].name : ''
  })(),
}))
</script>

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
