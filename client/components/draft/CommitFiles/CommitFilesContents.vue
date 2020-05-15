<template>
  <v-list v-if="commitFiles" dense>
    <v-list-item-group v-model="index">
      <v-list-item
        v-for="(file, i) in commitFiles"
        :key="i"
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
    </v-list-item-group>
  </v-list>
</template>

<script lang="ts">
import {
  defineComponent,
  computed,
  ref,
  watch,
  PropType,
} from '@vue/composition-api'
import { DiffCategory, CommitFile } from 'refactorhub'
import { trimFileName } from '@/components/common/editor/use/trim'

export default defineComponent({
  name: 'CommitFilesContents',
  props: {
    category: {
      type: String as PropType<DiffCategory>,
      required: true,
    },
  },
  setup(props, { root }) {
    const index = ref<number>()
    const commitFiles = computed(() => root.$accessor.draft.commitInfo?.files)

    const onClickItem = (value: number) => {
      root.$accessor.draft.setDisplayedFileMetadata({
        category: props.category,
        metadata: { index: value },
      })
    }

    watch(
      () => root.$accessor.draft.displayedFileMetadata[props.category]?.index,
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
      commitFiles,
      onClickItem,
      getFileName,
      isDisabled,
      trimFileName,
    }
  },
})
</script>
