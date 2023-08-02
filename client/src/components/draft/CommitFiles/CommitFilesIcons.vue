<script setup lang="ts">
import {
  mdiArrowRightBoldBox,
  mdiMinusBox,
  mdiPencilBox,
  mdiPlusBox,
} from '@mdi/js'
import { CommitFile, ActionName, ActionType } from '@/apis'
import { log } from '@/utils/action'

defineProps({
  files: {
    type: Array as () => CommitFile[],
    required: true,
  },
})

const { displayedFile } = useDraft()
const index = ref<number>()

const onClickItem = (value: number) => {
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
  () => displayedFile.value.before,
  (value) => {
    if (value?.index === displayedFile.value.after?.index) {
      index.value = value?.index
    } else index.value = undefined
  },
)

watch(
  () => displayedFile.value.after,
  (value) => {
    if (value?.index === displayedFile.value.before?.index) {
      index.value = value?.index
    } else index.value = undefined
  },
)
</script>

<template>
  <v-list class="icon-list">
    <div v-for="(file, i) in files" :key="i">
      <v-list-item :disabled="i === index" @click="onClickItem(i)">
        <v-icon
          v-if="file.status === 'modified'"
          size="small"
          :icon="mdiPencilBox"
          color="amber"
        />
        <v-icon
          v-if="file.status === 'added'"
          size="small"
          :icon="mdiPlusBox"
          color="green"
        />
        <v-icon
          v-if="file.status === 'removed'"
          size="small"
          :icon="mdiMinusBox"
          color="red"
        />
        <v-icon
          v-if="file.status === 'renamed'"
          size="small"
          :icon="mdiArrowRightBoldBox"
          color="purple"
        />
      </v-list-item>
      <v-divider />
    </div>
  </v-list>
</template>

<style lang="scss" scoped>
.icon-list {
  padding: 0;
}
</style>
