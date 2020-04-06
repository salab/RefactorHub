<template>
  <div id="container">
    <loading :active.sync="isLoading" :is-full-page="false" />
  </div>
</template>

<script lang="ts">
import { Vue, Component } from 'nuxt-property-decorator'
import * as monaco from 'monaco-editor'
import Loading from 'vue-loading-overlay'
import 'vue-loading-overlay/dist/vue-loading.css'
import { Diff, Element } from 'refactorhub'
import { cloneDeep } from 'lodash'
import { Debounce } from 'lodash-decorators'

@Component({
  components: {
    Loading,
  },
})
export default class MonacoEditor extends Vue {
  public diffEditor!: monaco.editor.IStandaloneDiffEditor

  private pending = 0

  private widgets: {
    [key in Diff]: (monaco.editor.IContentWidget & { type: string })[]
  } = {
    before: [],
    after: [],
  }

  private statements: {
    [key in Diff]: Element[]
  } = {
    before: [],
    after: [],
  }

  private listeners: {
    [key in Diff]: monaco.IDisposable[]
  } = {
    before: [],
    after: [],
  }

  private mounted() {
    const container = document.getElementById('container')
    if (container !== null) {
      this.diffEditor = monaco.editor.createDiffEditor(container, {
        enableSplitViewResizing: false,
        automaticLayout: true,
        readOnly: true,
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

    const uri = this.$editor.getCommitUri(owner, repository, sha, path)
    const text = await this.$accessor.draft.getTextModel({
      owner,
      repository,
      sha,
      path,
      uri: uri.toString(),
    })
    const model =
      monaco.editor.getModel(uri) ||
      monaco.editor.createModel(text.value, text.language, uri)

    if (diff === 'before') {
      this.diffEditor.setModel({
        original: model,
        modified:
          this.diffEditor.getModifiedEditor().getModel() ||
          monaco.editor.createModel('', 'text/plain'),
      })
    } else if (diff === 'after') {
      this.diffEditor.setModel({
        original:
          this.diffEditor.getOriginalEditor().getModel() ||
          monaco.editor.createModel('', 'text/plain'),
        modified: model,
      })
    }

    const editor = this.getEditor(diff)

    // Clear element widgets
    this.widgets[diff].forEach((widget) => {
      editor.removeContentWidget(widget)
    })

    text.elements.forEach((element, i) => {
      if (element.type === 'Statements') {
        this.statements[diff].push(element)
      } else {
        const widget = this.$editor.createElementWidget(
          diff,
          editor,
          element,
          i,
          async () => {
            const draft = this.$accessor.draft.draft
            const item = this.$accessor.draft.element[diff]
            if (draft && item) {
              const key = item[0]
              this.$accessor.draft.setDraft(
                await this.$client.updateElement(draft.id, diff, key, element)
              )
              this.deleteElementDecoration(diff, key)
              this.setElementDecoration(diff, key, element)
              this.$accessor.draft.setElement({ diff })
            }
          }
        )
        editor.addContentWidget(widget)
        this.widgets[diff].push(widget)
      }
    })

    this.pending--
  }

  public showElementWidgets(diff: Diff, type: string) {
    this.widgets[diff].forEach((widget) => {
      if (widget.type === type) widget.getDomNode().style.display = 'block'
      else widget.getDomNode().style.display = 'none'
    })
  }

  public hideElementWidgets(diff: Diff) {
    this.widgets[diff].forEach((widget) => {
      widget.getDomNode().style.display = 'none'
    })
  }

  public setupStatementsCursor(diff: Diff) {
    const editor = this.getEditor(diff)
    this.listeners[diff].push(
      editor.onDidChangeCursorSelection((e) => {
        this.updateStatements(diff, e.selection)
      })
    )
  }

  public disposeStatementsCursor(diff: Diff) {
    this.listeners[diff].forEach((listener) => listener.dispose())
  }

  @Debounce(500)
  public updateStatements(diff: Diff, range: monaco.Range) {
    this.statements[diff].forEach(async (element) => {
      if (
        this.$editor.asMonacoRange(element.location.range).containsRange(range)
      ) {
        const draft = this.$accessor.draft.draft
        const item = this.$accessor.draft.element[diff]
        if (draft && item) {
          const key = item[0]
          const newElement = cloneDeep(element)
          newElement.location.range = this.$editor.asRange(range)
          this.$accessor.draft.setDraft(
            await this.$client.updateElement(draft.id, diff, key, newElement)
          )
          this.deleteElementDecoration(diff, key)
          this.setElementDecoration(diff, key, newElement)
          this.$accessor.draft.setElement({ diff })
        }
      }
    })
  }

  public setElementDecoration(diff: Diff, key: string, element: Element) {
    const editor = this.getEditor(diff)
    const model = editor.getModel()
    if (model) {
      const [id] = editor.deltaDecorations(
        [],
        [this.$editor.createElementDecoration(key, element)]
      )
      this.$accessor.draft.decorations[diff].set(key, {
        id,
        uri: model.uri,
      })
    }
  }

  public deleteElementDecoration(diff: Diff, key: string) {
    const info = this.$accessor.draft.decorations[diff].get(key)
    if (info !== undefined) {
      const model = monaco.editor.getModel(info.uri)
      if (model !== null) model.deltaDecorations([info.id], [])
      this.$accessor.draft.deleteElementDecoration({ diff, key })
    }
  }

  private getEditor(diff: Diff) {
    return diff === 'before'
      ? this.diffEditor.getOriginalEditor()
      : this.diffEditor.getModifiedEditor()
  }
}
</script>

<style lang="scss" scoped>
#container {
  position: relative;
}
</style>

<style lang="scss">
#container {
  width: 100%;
  height: 100%;
}

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
