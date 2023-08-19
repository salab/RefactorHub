import * as monaco from 'monaco-editor'
import { DecorationMetadata, DiffCategory } from 'refactorhub'
import { TokenSequence } from 'composables/useCommonTokenSequence'

export class CommonTokenSequenceDecorationManager {
  private readonly currentDecorations: {
    [category in DiffCategory]: Set<{
      sequence: TokenSequence
      metadata: DecorationMetadata
    }>
  } = {
    before: new Set(),
    after: new Set(),
  }

  public setCommonTokensDecorations(
    path: string,
    category: DiffCategory,
    editor: monaco.editor.ICodeEditor,
  ) {
    const model = editor.getModel()
    if (!model) return
    const commonTokenSequences =
      useCommonTokenSequence().getCommonTokenSequencesIn(path, category)
    for (const { sequence, id, count } of commonTokenSequences) {
      const decoration = this.createCommonTokenSequenceDecoration(
        sequence,
        id,
        count,
      )
      const [decorationId] = editor.deltaDecorations([], [decoration])
      this.currentDecorations[category].add({
        sequence,
        metadata: {
          id: decorationId,
          uri: model.uri.toString(),
        },
      })
    }
  }

  public clearCommonTokensDecorations(category: DiffCategory) {
    this.currentDecorations[category].forEach(({ metadata }) => {
      const model = monaco.editor.getModel(monaco.Uri.parse(metadata.uri))
      if (model) model.deltaDecorations([metadata.id], [])
    })
    this.currentDecorations[category].clear()
  }

  private createCommonTokenSequenceDecoration(
    sequence: TokenSequence,
    id: number,
    count: { [category in DiffCategory]: number },
  ): monaco.editor.IModelDeltaDecoration {
    const hoverMessage = [
      {
        value: `**View Common Token Sequence (id=${id}): \`${sequence.joinedRaw}\` (before: ${count.before}, after: ${count.after})**`,
      },
    ]
    const className = `commonTokenSequence-decoration-${id}`
    return {
      range: sequence.range,
      options: {
        className,
        hoverMessage,
      },
    }
  }
}
