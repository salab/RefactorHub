<template>
  <div id="container" />
</template>

<script lang="ts">
import { Vue, Component } from 'nuxt-property-decorator'
import * as monaco from 'monaco-editor'

@Component
export default class MonacoEditor extends Vue {
  public diffEditor!: monaco.editor.IStandaloneDiffEditor

  private mounted() {
    const container = document.getElementById('container')
    if (container !== null) {
      this.diffEditor = monaco.editor.createDiffEditor(container, {
        enableSplitViewResizing: false,
        automaticLayout: true,
        readOnly: true,
        theme: 'vs-dark'
      })
    }
  }
}
</script>
