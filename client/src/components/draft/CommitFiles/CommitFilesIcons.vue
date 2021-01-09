<template>
  <v-list dense class="icon-list">
    <v-list-item-group v-model="index">
      <div v-for="(file, i) in files" :key="i">
        <v-list-item :disabled="i === index" @click="onClickItem(i)">
          <v-icon v-if="file.status === 'modified'" small color="amber">
            fa-fw fa-pen-square
          </v-icon>
          <v-icon v-if="file.status === 'added'" small color="green">
            fa-fw fa-plus-square
          </v-icon>
          <v-icon v-if="file.status === 'removed'" small color="red">
            fa-fw fa-minus-square
          </v-icon>
          <v-icon v-if="file.status === 'renamed'" small color="purple">
            fa-fw fa-caret-square-right
          </v-icon>
        </v-list-item>
        <v-divider />
      </div>
    </v-list-item-group>
  </v-list>
</template>

<script lang="ts">
import {
  defineComponent,
  ref,
  watch,
  useContext,
} from '@nuxtjs/composition-api'
import { CommitFile, LogEvent, LogType } from '@/apis'
import { log } from '@/utils/log'

export default defineComponent({
  props: {
    files: {
      type: Array as () => CommitFile[],
      required: true,
    },
  },
  setup() {
    const {
      app: { $accessor },
    } = useContext()

    const index = ref<number>()

    const onClickItem = (value: number) => {
      if ($accessor.draft.displayedFile.before?.index !== value) {
        log(LogEvent.SetDisplayedFile, LogType.Client, {
          category: 'before',
          file: { index: value },
        })
        $accessor.draft.setDisplayedFile({
          category: 'before',
          file: { index: value },
        })
      }
      if ($accessor.draft.displayedFile.after?.index !== value) {
        log(LogEvent.SetDisplayedFile, LogType.Client, {
          category: 'after',
          file: { index: value },
        })
        $accessor.draft.setDisplayedFile({
          category: 'after',
          file: { index: value },
        })
      }
    }

    watch(
      () => $accessor.draft.displayedFile.before,
      (value) => {
        if (value?.index === $accessor.draft.displayedFile.after?.index) {
          index.value = value?.index
        } else index.value = undefined
      }
    )

    watch(
      () => $accessor.draft.displayedFile.after,
      (value) => {
        if (value?.index === $accessor.draft.displayedFile.before?.index) {
          index.value = value?.index
        } else index.value = undefined
      }
    )

    return {
      index,
      onClickItem,
    }
  },
})
</script>

<style lang="scss" scoped>
.icon-list {
  padding: 0;
}
</style>
