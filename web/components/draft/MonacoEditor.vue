<template>
  <div id="container" class="fill-height">
    <loading :active.sync="isLoading" :is-full-page="false" />
  </div>
</template>

<script lang="ts">
import { Vue, Component } from 'nuxt-property-decorator'
import * as monaco from 'monaco-editor'
import Loading from 'vue-loading-overlay'
import 'vue-loading-overlay/dist/vue-loading.css'

@Component({
  components: {
    Loading
  }
})
export default class MonacoEditor extends Vue {
  public diffEditor!: monaco.editor.IStandaloneDiffEditor
  public isLoading = true

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

<style lang="scss" scoped>
#container {
  position: relative;
}
</style>