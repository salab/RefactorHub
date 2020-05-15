<template>
  <monaco-editor ref="editorRef" :is-loading="isLoading" />
</template>

<script lang="ts">
import { defineComponent, ref, watch, computed } from '@vue/composition-api'
import { DiffCategory, FileMetadata } from 'refactorhub'
import { changeDisplayedFileOnDiffEditor } from './use/displayedFile'
import { changeEditingElement } from './use/editingElement'
import MonacoEditor from '@/components/common/editor/MonacoEditor.vue'

export default defineComponent({
  name: 'ElementEditor',
  components: {
    MonacoEditor,
  },
  setup(_, { root }) {
    const editorRef = ref<InstanceType<typeof MonacoEditor>>()
    const pending = ref(0)
    const isLoading = computed(() => pending.value > 0)

    async function onChangeDisplayedFileMetadata(
      category: DiffCategory,
      metadata?: FileMetadata
    ) {
      if (!metadata) return
      const diffEditor = editorRef.value?.diffEditor
      if (!diffEditor) {
        console.log('diffEditor is not loaded')
        return
      }

      pending.value++
      await changeDisplayedFileOnDiffEditor(
        category,
        metadata,
        diffEditor,
        root.$accessor,
        root.$client
      )
      pending.value--
    }

    watch(
      () => root.$accessor.draft.displayedFileMetadata.before,
      (value) => onChangeDisplayedFileMetadata('before', value)
    )
    watch(
      () => root.$accessor.draft.displayedFileMetadata.after,
      (value) => onChangeDisplayedFileMetadata('after', value)
    )

    watch(
      () => root.$accessor.draft.editingElementMetadata.before,
      (value) => changeEditingElement('before', value)
    )
    watch(
      () => root.$accessor.draft.editingElementMetadata.after,
      (value) => changeEditingElement('after', value)
    )

    return {
      editorRef,
      isLoading,
    }
  },
})
</script>
