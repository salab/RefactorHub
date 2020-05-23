<template>
  <monaco-editor
    ref="editorRef"
    :is-loading="isLoading"
    class="element-editor"
  />
</template>

<script lang="ts">
import { defineComponent, ref, watch, computed } from '@vue/composition-api'
import consola from 'consola'
import { DiffCategory, FileMetadata } from 'refactorhub'
import { setupDisplayedFileOnDiffEditor } from './use/displayedFile'
import { setupEditingElement } from './use/editingElement'
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
        consola.log('diffEditor is not loaded')
        return
      }

      pending.value++
      await setupDisplayedFileOnDiffEditor(
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
      (value) => setupEditingElement('before', value)
    )
    watch(
      () => root.$accessor.draft.editingElementMetadata.after,
      (value) => setupEditingElement('after', value)
    )

    return {
      editorRef,
      isLoading,
    }
  },
})
</script>

<style lang="scss" scoped>
.element-editor ::v-deep {
  .element-widget {
    cursor: pointer;
    border: 2px solid;
    opacity: 0.6;
    &:hover {
      opacity: 1;
    }
  }
  .element-decoration {
    border: 1px solid;
  }
}
</style>
