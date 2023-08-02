<script setup lang="ts">
import { DiffCategory } from 'refactorhub'
import { CommitFile, ActionName, ActionType } from '@/apis'
import { trimFileName } from '@/components/common/editor/utils/trim'
import { log } from '@/utils/action'

const props = defineProps({
  category: {
    type: String as () => DiffCategory,
    required: true,
  },
  files: {
    type: Array as () => CommitFile[],
    required: true,
  },
})
const { displayedFile } = useDraft()

const index = ref<number>()

const onClickItem = (value: number) => {
  // log(ActionName.SetDisplayedFile, ActionType.Client, {
  //   category: props.category,
  //   file: { index: value },
  // })
  // useDraft().displayedFile.value[props.category] = { index: value }
  if (displayedFile.value.before?.index !== value) {
    log(ActionName.SetDisplayedFile, ActionType.Client, {
      category: 'before',
      file: { index: value },
    })
    displayedFile.value.before = { index: value }
  }
  if (displayedFile.value.after?.index !== value) {
    log(ActionName.SetDisplayedFile, ActionType.Client, {
      category: 'after',
      file: { index: value },
    })
    displayedFile.value.after = { index: value }
  }
}

watch(
  () => displayedFile.value[props.category]?.index,
  (value) => {
    index.value = value
  },
)

const getFileName = (file: CommitFile) =>
  props.category === 'before' ? file.previousName : file.name

const isDisabled = (file: CommitFile) =>
  props.category === 'before'
    ? file.status === 'added'
    : file.status === 'removed'
</script>

<template>
  <v-list class="file-list">
    <div v-for="(file, i) in files" :key="i">
      <v-list-item
        :disabled="isDisabled(file) || i === index"
        @click="onClickItem(i)"
      >
        <v-list-item-title v-if="!isDisabled(file)" :title="getFileName(file)">
          {{ trimFileName(getFileName(file), 50) }}
        </v-list-item-title>
      </v-list-item>
      <v-divider />
    </div>
  </v-list>
</template>

<style lang="scss" scoped>
.file-list {
  padding: 0;
}
</style>
