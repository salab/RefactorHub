<template>
  <monaco-editor ref="editorRef" :is-loading="isLoading" />
</template>

<script lang="ts">
import { defineComponent, ref, watch, computed } from '@vue/composition-api'
import * as monaco from 'monaco-editor'
import { DiffCategory, FileMetadata } from 'refactorhub'
import {
  setElementWidgetOnEditor,
  clearElementWidgetsOnEditor,
} from './use/elementWidgets'
import MonacoEditor from '@/components/common/editor/MonacoEditor.vue'
import { accessorType } from '@/store'
import { Client } from '@/plugins/client'

const editorRef = ref<InstanceType<typeof MonacoEditor>>()

export default defineComponent({
  name: 'ElementEditor',
  components: {
    MonacoEditor,
  },
  setup(_, { root }) {
    /*
    watch(
      () => root.$accessor.draft.editingElementMetadata.before,
      (value) => changeEditingElement('before', value)
    )
    watch(
      () => root.$accessor.draft.editingElementMetadata.after,
      (value) => changeEditingElement('after', value)
    )
    */

    const pending = ref(0)
    const isLoading = computed(() => pending.value > 0)

    watch(
      () => root.$accessor.draft.displayedFileMetadata.before,
      async (value) => {
        pending.value++
        await onChangeDisplayedFile(
          'before',
          value,
          root.$accessor,
          root.$client
        )
        pending.value--
      }
    )
    watch(
      () => root.$accessor.draft.displayedFileMetadata.after,
      async (value) => {
        pending.value++
        await onChangeDisplayedFile(
          'after',
          value,
          root.$accessor,
          root.$client
        )
        pending.value--
      }
    )

    return {
      editorRef,
      isLoading,
    }
  },
})

/*
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
*/

async function onChangeDisplayedFile(
  category: DiffCategory,
  metadata: FileMetadata,
  $accessor: typeof accessorType,
  $client: Client
) {
  const draft = $accessor.draft.draft
  const commitInfo = $accessor.draft.commitInfo
  if (!draft || !commitInfo) return

  const file = commitInfo.files[metadata.index]
  const sha = category === 'before' ? commitInfo.parent : commitInfo.sha
  const path = category === 'before' ? file.previousName : file.name
  const content = await $accessor.draft.getFileContent({
    owner: commitInfo.owner,
    repository: commitInfo.repository,
    sha,
    path,
    uri: getGitHubCommitUri(
      commitInfo.owner,
      commitInfo.repository,
      commitInfo.sha,
      path
    ),
  })
  const textModel =
    monaco.editor.getModel(monaco.Uri.parse(content.uri)) ||
    monaco.editor.createModel(
      content.value,
      content.language,
      monaco.Uri.parse(content.uri)
    )
  setTextModelOnDiffEditor(category, textModel)

  Object.entries(draft.data[category]).forEach(([key, data]) => {
    data.elements.forEach((element, index) => {
      if (path === element.location.path) {
        console.log(category, key, index, element, editor)
        // setElementDecoration(category, key, index, element, editor)
      }
    })
  })

  const editor = getEditor(category)
  if (!editor) return

  clearElementWidgetsOnEditor(category, editor)
  content.elements.forEach((element) => {
    setElementWidgetOnEditor(category, element, editor, $accessor, $client)
  })
}

function setTextModelOnDiffEditor(
  category: DiffCategory,
  textModel: monaco.editor.ITextModel
) {
  const diffEditor = editorRef.value?.diffEditor
  if (!diffEditor) return
  if (category === 'before') {
    diffEditor.setModel({
      original: textModel,
      modified:
        diffEditor.getModifiedEditor().getModel() ||
        monaco.editor.createModel('', 'text/plain'),
    })
  } else if (category === 'after') {
    diffEditor.setModel({
      original:
        diffEditor.getOriginalEditor().getModel() ||
        monaco.editor.createModel('', 'text/plain'),
      modified: textModel,
    })
  }
}

function getGitHubCommitUri(
  owner: string,
  repository: string,
  sha: string,
  path: string
) {
  return `https://github.com/${owner}/${repository}/blob/${sha}/${path}`
}

function getEditor(category: DiffCategory) {
  return category === 'before'
    ? editorRef.value?.diffEditor?.getOriginalEditor()
    : editorRef.value?.diffEditor?.getModifiedEditor()
}
</script>
