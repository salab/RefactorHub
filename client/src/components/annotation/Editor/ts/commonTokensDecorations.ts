import * as monaco from 'monaco-editor'
import { DiffCategory } from 'refactorhub'
import { TokenSequence } from 'composables/useCommonTokenSequence'
import { PathPair } from 'composables/useAnnotation'

/** Dependencies: beforePath, afterPath */
export class CommonTokenSequenceDecorationManager {
  private readonly viewer: {
    [category in DiffCategory]?: monaco.editor.ICodeEditor
  }

  private pathPair: PathPair

  private readonly decorationsCollection: {
    [category in DiffCategory]?: monaco.editor.IEditorDecorationsCollection
  } = {
    before: undefined,
    after: undefined,
  }

  public constructor(
    pathPair: PathPair,
    originalViewer: monaco.editor.ICodeEditor | undefined,
    modifiedViewer: monaco.editor.ICodeEditor | undefined,
  ) {
    this.pathPair = pathPair
    this.viewer = {
      before: originalViewer,
      after: modifiedViewer,
    }

    this.updateDecorations('before')
    this.updateDecorations('after')
    watch(
      () => useCommonTokenSequence().setting.value,
      () => {
        this.updateDecorations('before')
        this.updateDecorations('after')
      },
    )
  }

  public update(pathPair: PathPair) {
    this.pathPair = pathPair
    this.updateDecorations('before')
    this.updateDecorations('after')
  }

  private updateDecorations(category: DiffCategory) {
    this.decorationsCollection[category]?.clear()
    const viewer = this.viewer[category]
    const model = viewer?.getModel()
    const path = this.pathPair[category]
    if (!viewer || !model || path === undefined) return
    const commonTokenSequences =
      useCommonTokenSequence().getCommonTokenSequencesIn(path, category)
    this.decorationsCollection[category] = viewer.createDecorationsCollection(
      commonTokenSequences.map(({ sequence, id, count }) =>
        this.createCommonTokenSequenceDecoration(sequence, id, count),
      ),
    )
  }

  private createCommonTokenSequenceDecoration(
    sequence: TokenSequence,
    id: number,
    count: { [category in DiffCategory]: number },
  ): monaco.editor.IModelDeltaDecoration {
    const className = `commonTokenSequence-decoration-${id}`
    return {
      range: sequence.range,
      options: {
        className,
        hoverMessage: [
          {
            value: `\`${sequence.joinedRaw}\` (${count.before}:${count.after}) (id=${id})`,
          },
        ],
      },
    }
  }

  public getIdFromHoverMessageClickEvent(
    e: monaco.editor.IEditorMouseEvent,
  ): number | undefined {
    if (e.target.type !== monaco.editor.MouseTargetType.CONTENT_WIDGET)
      return undefined
    let element = e.target.element
    while (element) {
      const content = element.textContent ?? ''
      const id = this.getIdFromHoverMessage(content)
      if (id !== undefined) return id
      element = element.parentElement
    }
  }

  private getIdFromHoverMessage(content: string): number | undefined {
    if (!content.includes('(id=')) return undefined
    const firstIndex = content.lastIndexOf('(id=') + '(id='.length
    const lastIndex = content.lastIndexOf(')')
    const commonTokenSequenceId = Number.parseInt(
      content.substring(firstIndex, lastIndex),
    )
    return commonTokenSequenceId
  }
}
