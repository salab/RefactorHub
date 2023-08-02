<script setup lang="ts">
import * as monaco from 'monaco-editor'
import cryptoRandomString from 'crypto-random-string'

defineProps({
  isLoading: {
    type: Boolean,
    default: false,
  },
})
const id = cryptoRandomString({ length: 10 })
const computeDiffWrapper: {
  computeDiff: () => monaco.editor.IDocumentDiff
} = {
  computeDiff: () => ({
    identical: false,
    quitEarly: false,
    changes: [],
    moves: [],
  }),
}

let diffEditor: monaco.editor.IStandaloneDiffEditor | undefined
onMounted(() => {
  const container = document.getElementById(id)
  if (container !== null) {
    const editor = monaco.editor.createDiffEditor(container, {
      enableSplitViewResizing: false,
      automaticLayout: true,
      readOnly: true,
      scrollBeyondLastLine: false,
      diffAlgorithm: {
        onDidChange: () => ({ dispose: () => {} }),
        computeDiff: () =>
          new Promise((resolve) => resolve(computeDiffWrapper.computeDiff())),
      },
    })
    diffEditor = editor
  }
})

defineExpose({
  getDiffEditor: () => diffEditor,
  computeDiffWrapper,
})
</script>

<template>
  <div :id="id" class="wh-100">
    <loading-circle :active="isLoading" />
  </div>
</template>

<style lang="scss" scoped>
.wh-100 {
  width: 100%;
  height: 100%;
}
</style>
