<template>
  <v-dialog v-model="dialog" width="80%">
    <div style="height: 400px;">
      <monaco-editor ref="editorRef" />
    </div>
  </v-dialog>
</template>

<script lang="ts">
import * as monaco from 'monaco-editor'
import { defineComponent, ref, watch } from '@vue/composition-api'
import consola from 'consola'
import { DiffCategory } from 'refactorhub'
import MonacoEditor from '@/components/common/editor/MonacoEditor.vue'

const dialog = ref(false)
const contents = ref<{ [category in DiffCategory]: string }>({
  before: '',
  after: '',
})

export const useCodeFragmentDiff = () => {
  const open = () => {
    dialog.value = true
  }
  const setContents = (value: { [category in DiffCategory]: string }) => {
    contents.value = value
  }
  return { open, setContents }
}

export default defineComponent({
  name: 'CodeFragmentDiff',
  components: {
    MonacoEditor,
  },
  setup() {
    const editorRef = ref<InstanceType<typeof MonacoEditor>>()

    watch(contents, () => {
      const diffEditor = editorRef.value?.diffEditor
      if (!diffEditor) {
        consola.log('diffEditor is not loaded')
        return
      }
      diffEditor.setModel({
        original: monaco.editor.createModel(
          contents.value.before,
          'text/plain'
        ),
        modified: monaco.editor.createModel(contents.value.after, 'text/plain'),
      })
    })

    return {
      editorRef,
      dialog,
    }
  },
})
</script>
