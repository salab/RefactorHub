<script setup lang="ts">
import * as monaco from 'monaco-editor'
import { DiffCategory } from 'refactorhub'
import MonacoEditor from '@/components/common/editor/MonacoEditor.vue'

const dialog = ref(false)
const contents = ref<{ [category in DiffCategory]: string }>({
  before: '',
  after: '',
})

const useCodeFragmentDiff = () => {
  const open = () => {
    dialog.value = true
  }
  const setContents = (value: { [category in DiffCategory]: string }) => {
    contents.value = value
  }
  return { open, setContents }
}

const editorRef = ref<InstanceType<typeof MonacoEditor>>()

watch(contents, () => {
  editorRef.value?.getDiffEditor()?.setModel({
    original: monaco.editor.createModel(contents.value.before, 'text/plain'),
    modified: monaco.editor.createModel(contents.value.after, 'text/plain'),
  })
})

defineExpose({
  useCodeFragmentDiff,
})
</script>

<template>
  <v-dialog v-model="dialog" eager width="80%">
    <div :class="$style.container">
      <monaco-editor ref="editorRef" />
    </div>
  </v-dialog>
</template>

<style lang="scss" module>
.container {
  height: 400px;
}
</style>
