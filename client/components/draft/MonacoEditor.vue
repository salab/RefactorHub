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
import { Diff, Element } from 'refactorhub'

@Component({
  components: {
    Loading
  }
})
export default class MonacoEditor extends Vue {
  public diffEditor!: monaco.editor.IStandaloneDiffEditor
  private pending = 0
  private widgets: {
    before: (monaco.editor.IContentWidget & { type: string })[]
    after: (monaco.editor.IContentWidget & { type: string })[]
  } = {
    before: [],
    after: []
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
    diff: Diff,
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

    if (diff === 'before') {
      this.diffEditor.setModel({
        original: model,
        modified:
          this.diffEditor.getModifiedEditor().getModel() ||
          monaco.editor.createModel('', 'text/plain')
      })
    } else if (diff === 'after') {
      this.diffEditor.setModel({
        original:
          this.diffEditor.getOriginalEditor().getModel() ||
          monaco.editor.createModel('', 'text/plain'),
        modified: model
      })
    }
    const editor =
      diff === 'before'
        ? this.diffEditor.getOriginalEditor()
        : this.diffEditor.getModifiedEditor()
    this.widgets[diff].forEach(widget => {
      editor.removeContentWidget(widget)
    })
    text.elements.forEach((element, i) => {
      const widget = this.$editor.createWidget(
        editor,
        element,
        i,
        diff,
        async () => {
          const draft = this.$accessor.draft.draft
          const item = this.$accessor.draft.element[diff]
          if (draft && item) {
            const key = item[0]
            this.$accessor.draft.setDraft(
              await this.$client.updateElement(draft.id, diff, key, element)
            )
            this.deleteDecoration(diff, key)
            this.setDecoration(diff, key, element)
            this.$accessor.draft.setElement({ diff })
          }
        }
      )
      editor.addContentWidget(widget)
      this.widgets[diff].push(widget)
    })

    this.pending--
  }

  public showWidgets(diff: Diff, type: string) {
    this.widgets[diff].forEach(widget => {
      if (widget.type === type) widget.getDomNode().style.display = 'block'
      else widget.getDomNode().style.display = 'none'
    })
  }

  public hideWidgets(diff: Diff) {
    this.widgets[diff].forEach(widget => {
      widget.getDomNode().style.display = 'none'
    })
  }

  public setDecoration(diff: Diff, key: string, element: Element) {
    const editor =
      diff === 'before'
        ? this.diffEditor.getOriginalEditor()
        : this.diffEditor.getModifiedEditor()
    const model = editor.getModel()
    if (model) {
      const [id] = editor.deltaDecorations(
        [],
        [this.$editor.createDecoration(key, element)]
      )
      this.$accessor.draft.decorations[diff].set(key, {
        id,
        uri: model.uri
      })
    }
  }

  public deleteDecoration(diff: Diff, key: string) {
    const info = this.$accessor.draft.decorations[diff].get(key)
    if (info !== undefined) {
      const model = monaco.editor.getModel(info.uri)
      if (model !== null) model.deltaDecorations([info.id], [])
      this.$accessor.draft.deleteDecoration({ diff, key })
    }
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
  cursor: pointer;
  border: 2px solid;
  opacity: 0.6;
  &:hover {
    opacity: 1;
  }
}
.element-decoration {
  border: 1px solid;
}
</style>