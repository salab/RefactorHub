import * as monaco from 'monaco-editor'
import hash from 'object-hash'
import { Element, DiffCategory, DecorationMetadata } from 'refactorhub'
import { asMonacoRange } from '@/components/common/editor/utlis/range'

const decorationMetadataMap: {
  [category in DiffCategory]: Map<string, DecorationMetadata>
} = {
  before: new Map(),
  after: new Map(),
}

export function initElementDecorations() {
  decorationMetadataMap.before.clear()
  decorationMetadataMap.after.clear()
}

export function setElementDecorationOnEditor(
  category: DiffCategory,
  key: string,
  index: number,
  element: Element,
  editor: monaco.editor.ICodeEditor
) {
  const model = editor.getModel()
  if (model) {
    const [id] = editor.deltaDecorations(
      [],
      [createElementDecoration(key, element)]
    )
    decorationMetadataMap[category].set(hash({ key, index }), {
      id,
      uri: model.uri.toString(),
    })
  }
}

export function deleteElementDecoration(
  category: DiffCategory,
  key: string,
  index: number
) {
  const metadata = decorationMetadataMap[category].get(hash({ key, index }))
  if (metadata) {
    const model = monaco.editor.getModel(monaco.Uri.parse(metadata.uri))
    if (model) model.deltaDecorations([metadata.id], [])
    decorationMetadataMap[category].delete(hash({ key, index }))
  }
}

export function clearElementDecorations(category: DiffCategory) {
  decorationMetadataMap[category].forEach((metadata) => {
    const model = monaco.editor.getModel(monaco.Uri.parse(metadata.uri))
    if (model) model.deltaDecorations([metadata.id], [])
  })
  decorationMetadataMap[category].clear()
}

function createElementDecoration(
  key: string,
  element: Element
): monaco.editor.IModelDeltaDecoration {
  return {
    range: asMonacoRange(element.location.range),
    options: {
      className: `element-decoration element-decoration-${element.type}`,
      hoverMessage: { value: key },
    },
  }
}
