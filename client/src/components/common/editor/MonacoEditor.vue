<template>
  <div :id="id" class="wh-100">
    <loading :active="isLoading" />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted } from '@nuxtjs/composition-api'
import * as monaco from 'monaco-editor'
import cryptoRandomString from 'crypto-random-string'

export default defineComponent({
  props: {
    isLoading: {
      type: Boolean,
      default: false,
    },
  },
  setup() {
    const id = cryptoRandomString({ length: 10 })
    const diffEditor = ref<monaco.editor.IStandaloneDiffEditor>()
    const computeDiffWrapper: {
      computeDiff: () => monaco.editor.IDocumentDiff
    } = {
      computeDiff: () => ({
        identical: false,
        quitEarly: false,
        changes: [],
      }),
    }

    onMounted(() => {
      const container = document.getElementById(id)
      if (container !== null) {
        diffEditor.value = monaco.editor.createDiffEditor(container, {
          enableSplitViewResizing: false,
          automaticLayout: true,
          readOnly: true,
          scrollBeyondLastLine: false,
          diffAlgorithm: {
            onDidChange: () => ({ dispose: () => {} }),
            computeDiff: () =>
              new Promise((resolve) =>
                resolve(computeDiffWrapper.computeDiff())
              ),
          },
        })
      }
    })

    return {
      id,
      diffEditor,
      computeDiffWrapper,
    }
  },
})
</script>

<style lang="scss" scoped>
.wh-100 {
  width: 100%;
  height: 100%;
}
</style>
