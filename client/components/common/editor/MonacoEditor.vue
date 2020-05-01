<template>
  <div :id="id" class="wh-100">
    <loading :active="isLoading" />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted } from '@vue/composition-api'
import * as monaco from 'monaco-editor'
import cryptoRandomString from 'crypto-random-string'
import Loading from '@/components/common/editor/Loading.vue'

export default defineComponent({
  name: 'MonacoEditor',
  components: {
    Loading,
  },
  props: {
    isLoading: {
      type: Boolean,
      required: true,
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
