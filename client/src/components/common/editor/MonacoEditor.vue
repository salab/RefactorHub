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

    onMounted(() => {
      const container = document.getElementById(id)
      if (container !== null) {
        diffEditor.value = monaco.editor.createDiffEditor(container, {
          enableSplitViewResizing: false,
          automaticLayout: true,
          readOnly: true,
          scrollBeyondLastLine: false,
        })
      }
    })

    return {
      id,
      diffEditor,
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
