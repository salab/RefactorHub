import * as monaco from 'monaco-editor'
import { Range } from '@/apis'

export function asMonacoRange(range?: Range): monaco.Range {
  if (!range) {
    return new monaco.Range(0, 0, 0, 0)
  }
  return new monaco.Range(
    range.startLine,
    range.startColumn,
    range.endLine,
    range.endColumn + 1,
  )
}

export function asRange(range: monaco.Range): Range {
  return {
    startLine: range.startLineNumber,
    startColumn: range.startColumn,
    endLine: range.endLineNumber,
    endColumn: range.endColumn - 1,
  }
}

export function getRangeWidthOnEditor(
  range: monaco.Range,
  editor: monaco.editor.ICodeEditor,
) {
  const width = editor.getOption(
    monaco.editor.EditorOptions.fontInfo.id,
  ).typicalHalfwidthCharacterWidth
  if (range.startLineNumber === range.endLineNumber) {
    return width * (range.endColumn - range.startColumn)
  }
  return (
    width *
    (getMaxColumnOnEditor(range.startLineNumber, range.endLineNumber, editor) -
      (range.startColumn < range.endColumn - 1
        ? range.startColumn
        : range.endColumn - 1))
  )
}

function getMaxColumnOnEditor(
  startLineNumber: number,
  endLineNumber: number,
  editor: monaco.editor.ICodeEditor,
) {
  const model = editor.getModel()
  if (!model) return 0
  let column = 0
  for (let i = startLineNumber; i <= endLineNumber; i++) {
    column = Math.max(column, model.getLineLength(i) + 1)
  }
  return column
}

export function getRangeHeightOnEditor(
  range: monaco.Range,
  editor: monaco.editor.ICodeEditor,
) {
  return (
    editor.getOption(monaco.editor.EditorOptions.fontInfo.id).lineHeight +
    editor.getTopForLineNumber(range.endLineNumber) -
    editor.getTopForLineNumber(range.startLineNumber)
  )
}
