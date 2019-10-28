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
  private pending = 0
  private widgets: {
    original: (monaco.editor.IContentWidget & { type: string })[]
    modified: (monaco.editor.IContentWidget & { type: string })[]
  } = {
    original: [],
    modified: []
  }

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

  public get isLoading() {
    return this.pending > 0
  }

  public async setTextModel(
    which: 'original' | 'modified',
    owner: string,
    repository: string,
    sha: string,
    path: string
  ) {
    this.pending++

    const uri = this.$editor.getUri(owner, repository, sha, path)
    const text = await this.$accessor.draft.getTextModel({
      owner,
      repository,
      sha,
      path,
      uri: uri.toString()
    })
    let model = monaco.editor.getModel(uri)
    if (!model) {
      model = monaco.editor.createModel(text.value, text.language, uri)
    }

    if (which === 'original') {
      this.diffEditor.setModel({
        original: model,
        modified:
          this.diffEditor.getModifiedEditor().getModel() ||
          monaco.editor.createModel('', 'text/plain')
      })
      const editor = this.diffEditor.getOriginalEditor()
      this.widgets.original.forEach(widget => {
        editor.removeContentWidget(widget)
      })
      text.elements.forEach((element, i) => {
        const widget = this.$editor.createWidget(editor, element, i, 'original')
        editor.addContentWidget(widget)
        this.widgets.original.push(widget)
      })
    } else if (which === 'modified') {
      this.diffEditor.setModel({
        original:
          this.diffEditor.getOriginalEditor().getModel() ||
          monaco.editor.createModel('', 'text/plain'),
        modified: model
      })
      const editor = this.diffEditor.getModifiedEditor()
      this.widgets.modified.forEach(widget => {
        editor.removeContentWidget(widget)
      })
      text.elements.forEach((element, i) => {
        const widget = this.$editor.createWidget(editor, element, i, 'modified')
        editor.addContentWidget(widget)
        this.widgets.modified.push(widget)
      })
    }

    this.pending--
  }
}
</script>

<style lang="scss" scoped>
#container {
  position: relative;
}
</style>

<style lang="scss">
.element-widget {
  border: 2px solid;
}
</style>