<template>
  <v-list v-if="commitFiles" dense class="icon-list">
    <v-list-item-group v-model="index">
      <div v-for="(file, i) in commitFiles" :key="i">
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
import { defineComponent, computed, ref, watch } from '@nuxtjs/composition-api'

export default defineComponent({
  name: 'CommitFilesIcons',
  setup(_, { root }) {
    const index = ref<number>()
    const commitFiles = computed(() => root.$accessor.draft.commitInfo?.files)

    const onClickItem = (value: number) => {
      if (root.$accessor.draft.displayedFileMetadata.before?.index !== value) {
        root.$accessor.draft.setDisplayedFileMetadata({
          category: 'before',
          metadata: { index: value },
        })
      }
      if (root.$accessor.draft.displayedFileMetadata.after?.index !== value) {
        root.$accessor.draft.setDisplayedFileMetadata({
          category: 'after',
          metadata: { index: value },
        })
      }
    }

    watch(
      () => root.$accessor.draft.displayedFileMetadata.before,
      (value) => {
        if (
          value?.index ===
          root.$accessor.draft.displayedFileMetadata.after?.index
        ) {
          index.value = value?.index
        } else index.value = undefined
      }
    )

    watch(
      () => root.$accessor.draft.displayedFileMetadata.after,
      (value) => {
        if (
          value?.index ===
          root.$accessor.draft.displayedFileMetadata.before?.index
        ) {
          index.value = value?.index
        } else index.value = undefined
      }
    )

    return {
      index,
      commitFiles,
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
