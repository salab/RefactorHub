<template>
  <v-list v-if="commitFiles" dense class="file-list">
    <v-list-item-group v-model="index">
      <div v-for="(file, i) in commitFiles" :key="i">
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
import { defineComponent, computed, ref, watch } from '@nuxtjs/composition-api'
import { DiffCategory, CommitFile } from 'refactorhub'
import { trimFileName } from '@/components/common/editor/use/trim'

export default defineComponent({
  name: 'CommitFilesContents',
  props: {
    category: {
      type: String as () => DiffCategory,
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

<style lang="scss" scoped>
.file-list {
  padding: 0;
}
</style>
