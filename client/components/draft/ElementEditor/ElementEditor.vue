<template>
  <monaco-editor ref="editorRef" :is-loading="true" />
</template>

<script lang="ts">
import { defineComponent, ref, watch, onMounted } from '@vue/composition-api'
import * as monaco from 'monaco-editor'
import {
  DiffCategory,
  ElementMetadata,
  FileMetadata,
  CommitInfo,
  Draft,
} from 'refactorhub'
import { hideElementWidgets, showElementWidgetsWithType } from './widgets'
import { setElementDecoration } from './use/decorations'
import MonacoEditor from '@/components/common/editor/MonacoEditor.vue'

const editorRef = ref<InstanceType<typeof MonacoEditor>>()

export default defineComponent({
  name: 'ElementEditor',
  components: {
    MonacoEditor,
  },
  setup(_, { root }) {
    watch(
      () => root.$accessor.draft.editingElementMetadata.before,
      (value) => changeEditingElement('before', value)
    )
    watch(
      () => root.$accessor.draft.editingElementMetadata.after,
      (value) => changeEditingElement('after', value)
    )

    watch(
      () => root.$accessor.draft.displayedFileMetadata.before,
      (value) => changeDisplayedFile('before', value)
    )
    watch(
      () => root.$accessor.draft.displayedFileMetadata.after,
      (value) => changeDisplayedFile('after', value)
    )
    return {
      editorRef,
    }
  },
})

function changeEditingElement(
  category: DiffCategory,
  metadata?: ElementMetadata
) {
  if (metadata !== undefined) {
    if (metadata.type === 'Statements') {
      setupStatementsCursor(category)
      hideElementWidgets(category)
    } else {
      showElementWidgets(category, metadata.type)
      disposeStatementsCursor(category)
    }
  } else {
    hideElementWidgets(category)
    disposeStatementsCursor(category)
  }
}

function changeDisplayedFile(
  category: DiffCategory,
  metadata: FileMetadata | undefined,
  draft: Draft | undefined,
  commitInfo: CommitInfo | undefined,
  editor: monaco.editor.ICodeEditor
) {
  if (commitInfo && metadata !== undefined) {
    const path =
      category === 'before'
        ? commitInfo.files[metadata.index].previousName
        : commitInfo.files[metadata.index].name
    editor.setTextModel(
      category,
      commitInfo.owner,
      commitInfo.repository,
      category === 'before' ? commitInfo.parent : commitInfo.sha,
      path
    )
    if (draft) {
      Object.entries(draft.data[category]).forEach(([key, data]) => {
        data.elements.forEach((element, index) => {
          if (path === element.location.path) {
            setElementDecoration(category, key, index, element, editor)
          }
        })
      })
    }
  }
}

function getEditor(category: DiffCategory) {
  return category === 'before'
    ? editorRef.value?.diffEditor?.getOriginalEditor()
    : editorRef.value?.diffEditor?.getModifiedEditor()
}
</script>
