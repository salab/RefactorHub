import * as monaco from 'monaco-editor'
import { Element, Diff } from 'refactorhub'
import { Context } from '@nuxt/types'
import { accessorType } from '~/store'

export class Editor {
  private $accessor: typeof accessorType

  constructor($accessor: typeof accessorType) {
    this.$accessor = $accessor
  }

  createDecoration(
    key: string,
    type: string,
    range: monaco.Range
  ): monaco.editor.IModelDeltaDecoration {
    return {
      range,
      options: {
        className: `highlight-${type}`,
        hoverMessage: { value: key }
      }
    }
  }

  createWidget(
    editor: monaco.editor.ICodeEditor,
    element: Element,
    index: number,
    diff: Diff
  ): monaco.editor.IContentWidget & { type: string } {
    const range = new monaco.Range(
      element.location.range.startLine,
      element.location.range.startColumn,
      element.location.range.endLine,
      element.location.range.endColumn + 1
    )
    const div = document.createElement('div')
    div.className = 'element-widget'
    div.style.width = `${this.getWidth(editor, range)}px`
    div.style.height = `${this.getHeight(editor, range)}px`
    div.style.minWidth = div.style.width
    div.style.backgroundColor = this.getColor(element.type, 0.2)
    div.style.borderColor = this.getColor(element.type, 0.9)
    div.style.display = 'none'
    setTimeout(() => {
      try {
        // Fix diff lines
        div.style.height = `${this.getHeight(editor, range)}px`
      } catch (e) {}
    }, 1000)
    div.addEventListener('click', () => {
      console.log(element.type)
    })
    return {
      type: `${element.type}`,
      getId: () => `widget-${diff}-${index}`,
      getDomNode: () => div,
      getPosition: () => ({
        position: {
          lineNumber: range.startLineNumber,
          column: range.startColumn
        },
        preference: [monaco.editor.ContentWidgetPositionPreference.EXACT]
      })
    }
  }

  getMaxColumn(
    editor: monaco.editor.ICodeEditor,
    startLineNumber: number,
    endLineNumber: number
  ) {
    const model = editor.getModel()
    if (!model) return 0
    let column = 0
    for (let i = startLineNumber; i <= endLineNumber; i++) {
      column = Math.max(column, model.getLineLength(i) + 1)
    }
    return column
  }

  getWidth(editor: monaco.editor.ICodeEditor, range: monaco.Range) {
    const width = editor.getConfiguration().fontInfo
      .typicalHalfwidthCharacterWidth
    if (range.startLineNumber === range.endLineNumber) {
      return width * (range.endColumn - range.startColumn)
    }
    return (
      width *
      (this.getMaxColumn(editor, range.startLineNumber, range.endLineNumber) -
        range.startColumn)
    )
  }

  getHeight(editor: monaco.editor.ICodeEditor, range: monaco.Range) {
    return (
      editor.getConfiguration().fontInfo.lineHeight +
      editor.getTopForLineNumber(range.endLineNumber) -
      editor.getTopForLineNumber(range.startLineNumber)
    )
  }

  getUri(owner: string, repository: string, sha: string, path: string) {
    return monaco.Uri.parse(
      `https://github.com/${owner}/${repository}/blob/${sha}/${path}`
    )
  }

  getColor(type: string, alpha = 1.0) {
    const types = this.$accessor.draft.elementTypes
    const length = types.length
    const index = types.indexOf(type)
    return `hsla(${(index * 360) / length}, 100%, 40%, ${alpha})`
  }
}

export default (
  { app }: Context,
  inject: (name: string, editor: Editor) => void
) => {
  inject('editor', new Editor(app.$accessor))
}
