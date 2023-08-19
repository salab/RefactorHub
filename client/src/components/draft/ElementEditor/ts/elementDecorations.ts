import * as monaco from 'monaco-editor'
import hash from 'object-hash'
import { DiffCategory, DecorationMetadata } from 'refactorhub'
import { asMonacoRange } from '@/components/common/editor/utils/range'
import { CodeElement } from '@/apis'

export class ElementDecorationManager {
  private readonly decorationMetadataMap: {
    [category in DiffCategory]: Map<string, DecorationMetadata>
  } = {
    before: new Map(),
    after: new Map(),
  }

  public setElementDecorationOnEditor(
    category: DiffCategory,
    key: string,
    index: number,
    element: CodeElement,
    editor: monaco.editor.ICodeEditor,
  ) {
    const model = editor.getModel()
    if (model) {
      const [id] = editor.deltaDecorations(
        [],
        [this.createElementDecoration(key, element)],
      )
      this.decorationMetadataMap[category].set(hash({ key, index }), {
        id,
        uri: model.uri.toString(),
      })
    }
  }

  public deleteElementDecoration(
    category: DiffCategory,
    key: string,
    index: number,
  ) {
    const metadata = this.decorationMetadataMap[category].get(
      hash({ key, index }),
    )
    if (metadata) {
      const model = monaco.editor.getModel(monaco.Uri.parse(metadata.uri))
      if (model) model.deltaDecorations([metadata.id], [])
      this.decorationMetadataMap[category].delete(hash({ key, index }))
    }
  }

  public clearElementDecorations(category: DiffCategory) {
    this.decorationMetadataMap[category].forEach((metadata) => {
      const model = monaco.editor.getModel(monaco.Uri.parse(metadata.uri))
      if (model) model.deltaDecorations([metadata.id], [])
    })
    this.decorationMetadataMap[category].clear()
  }

  private createElementDecoration(
    key: string,
    element: CodeElement,
  ): monaco.editor.IModelDeltaDecoration {
    return {
      range: asMonacoRange(element.location?.range),
      options: {
        className: `element-decoration element-decoration-${element.type}`,
        hoverMessage: { value: key },
      },
    }
  }
}
