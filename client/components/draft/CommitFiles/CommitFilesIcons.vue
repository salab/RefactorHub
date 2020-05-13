<template>
  <v-list v-if="commitFiles" dense>
    <v-list-item-group v-model="index">
      <v-list-item v-for="(file, i) in commitFiles" :key="i">
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
    </v-list-item-group>
  </v-list>
</template>

<script lang="ts">
import { defineComponent, computed, ref, watch } from '@vue/composition-api'

export default defineComponent({
  name: 'CommitFilesIcons',
  setup(_, { root }) {
    const index = ref(0)
    const commitFiles = computed(() => root.$accessor.draft.commitInfo?.files)

    watch(index, (value) => {
      root.$accessor.draft.setDisplayedFileMetadata({
        category: 'before',
        metadata: { index: value },
      })
      root.$accessor.draft.setDisplayedFileMetadata({
        category: 'after',
        metadata: { index: value },
      })
    })

    return {
      index,
      commitFiles,
    }
  },
})
</script>
