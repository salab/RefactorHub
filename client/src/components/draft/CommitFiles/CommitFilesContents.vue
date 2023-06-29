<template>
  <v-list dense class="file-list">
    <v-list-item-group v-model="index">
      <div v-for="(file, i) in files" :key="i">
        <v-list-item
          :disabled="isDisabled(file) || i === index"
          @click="onClickItem(i)"
        >
          <v-list-item-content>
            <v-list-item-title
              v-if="!isDisabled(file)"
              :title="getFileName(file)"
            >
              {{ trimFileName(getFileName(file), 50) }}
            </v-list-item-title>
          </v-list-item-content>
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
import { DiffCategory } from 'refactorhub'
import { CommitFile, ActionName, ActionType } from '@/apis'
import { trimFileName } from '@/components/common/editor/utils/trim'
import { log } from '@/utils/action'

export default defineComponent({
  props: {
    category: {
      type: String as () => DiffCategory,
      required: true,
    },
    files: {
      type: Array as () => CommitFile[],
      required: true,
    },
  },
  setup(props) {
    const {
      app: { $accessor },
    } = useContext()

    const index = ref<number>()

    const onClickItem = (value: number) => {
      // log(ActionName.SetDisplayedFile, ActionType.Client, {
      //   category: props.category,
      //   file: { index: value },
      // })
      // $accessor.draft.setDisplayedFile({
      //   category: props.category,
      //   file: { index: value },
      // })
      if ($accessor.draft.displayedFile.before?.index !== value) {
        log(ActionName.SetDisplayedFile, ActionType.Client, {
          category: 'before',
          file: { index: value },
        })
        $accessor.draft.setDisplayedFile({
          category: 'before',
          file: { index: value },
        })
      }
      if ($accessor.draft.displayedFile.after?.index !== value) {
        log(ActionName.SetDisplayedFile, ActionType.Client, {
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
      () => $accessor.draft.displayedFile[props.category]?.index,
      (value) => {
        index.value = value
      }
    )

    const getFileName = (file: CommitFile) =>
      props.category === 'before' ? file.previousName : file.name

    const isDisabled = (file: CommitFile) =>
      props.category === 'before'
        ? file.status === 'added'
        : file.status === 'removed'

    return {
      index,
      onClickItem,
      getFileName,
      isDisabled,
      trimFileName,
    }
  },
})
</script>

<style lang="scss" scoped>
.file-list {
  padding: 0;
}
</style>
