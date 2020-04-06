import * as monaco from 'monaco-editor'
import { Element, Diff, Range } from 'refactorhub'
import { Plugin } from '@nuxt/types'
import { accessorType } from '@/store'

export class Editor {
  private $accessor: typeof accessorType

  constructor($accessor: typeof accessorType) {
    this.$accessor = $accessor
  }

  createElementDecoration(
    key: string,
    element: Element
  ): monaco.editor.IModelDeltaDecoration {
    return {
      range: this.asMonacoRange(element.location.range),
      options: {
        className: `element-decoration element-decoration-${element.type}`,
        hoverMessage: { value: key },
      },
    }
  }

  createElementWidget(
    diff: Diff,
    editor: monaco.editor.ICodeEditor,
    element: Element,
    index: number,
    onClick: (e: Event) => void
  ): monaco.editor.IContentWidget & { type: string } {
    const range = this.asMonacoRange(element.location.range)
    const div = document.createElement('div')
    div.classList.add('element-widget')
    div.classList.add(`element-widget-${element.type}`)
    div.style.width = `${this.getRangeWidth(editor, range)}px`
    div.style.height = `${this.getRangeHeight(editor, range)}px`
    div.style.minWidth = div.style.width
    div.style.display = 'none'
    setTimeout(() => {
      try {
        // Fix diff lines
        div.style.height = `${this.getRangeHeight(editor, range)}px`
      } catch (e) {}
    }, 1000)
    div.addEventListener('click', onClick)
    return {
      type: `${element.type}`,
      getId: () => `widget-${diff}-${index}`,
      getDomNode: () => div,
      getPosition: () => ({
        position: {
          lineNumber: range.startLineNumber,
          column: range.startColumn,
        },
        preference: [monaco.editor.ContentWidgetPositionPreference.EXACT],
      }),
    }
  }

  private getRangeWidth(
    editor: monaco.editor.ICodeEditor,
    range: monaco.Range
  ) {
    const width = editor.getOption(monaco.editor.EditorOptions.fontInfo.id)
      .typicalFullwidthCharacterWidth
    if (range.startLineNumber === range.endLineNumber) {
      return width * (range.endColumn - range.startColumn)
    }
    return (
      width *
      (this.getMaxColumn(editor, range.startLineNumber, range.endLineNumber) -
        range.startColumn)
    )
  }

  private getMaxColumn(
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

  private getRangeHeight(
    editor: monaco.editor.ICodeEditor,
    range: monaco.Range
  ) {
    return (
      editor.getOption(monaco.editor.EditorOptions.fontInfo.id).lineHeight +
      editor.getTopForLineNumber(range.endLineNumber) -
      editor.getTopForLineNumber(range.startLineNumber)
    )
  }

  getCommitUri(owner: string, repository: string, sha: string, path: string) {
    return monaco.Uri.parse(
      `https://github.com/${owner}/${repository}/blob/${sha}/${path}`
    )
  }

  asMonacoRange(range: Range): monaco.Range {
    return new monaco.Range(
      range.startLine,
      range.startColumn,
      range.endLine,
      range.endColumn + 1
    )
  }

  asRange(range: monaco.Range): Range {
    return {
      startLine: range.startLineNumber,
      startColumn: range.startColumn,
      endLine: range.endLineNumber,
      endColumn: range.endColumn - 1,
    }
  }
}

const editor: Plugin = ({ app }, inject) => {
  inject('editor', new Editor(app.$accessor))
}

export default editor
