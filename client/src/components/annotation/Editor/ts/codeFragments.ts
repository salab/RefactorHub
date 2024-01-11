import * as monaco from 'monaco-editor'
import { cloneDeep, debounce } from 'lodash-es'
import { DiffCategory } from 'refactorhub'
import { asRange, asMonacoRange } from '@/components/common/editor/utils/range'
import apis, { CodeElement, CodeElementType } from '@/apis'
import { PathPair } from 'composables/useAnnotation'

/** Dependencies: beforePath, afterPath, beforeElements, afterElements, linesMap */
export class CodeFragmentManager {
  private pathPair: PathPair
  private readonly codeFragments: {
    [category in DiffCategory]: CodeElement[]
  }

  private readonly linesMap: {
    [category in DiffCategory]?: {
      [lineNumber: number]: number
    }
  } = {
    before: undefined,
    after: undefined,
  }

  public constructor(
    pathPair: PathPair,
    beforeElements: CodeElement[],
    afterElements: CodeElement[],
    originalViewer: monaco.editor.ICodeEditor | undefined,
    modifiedViewer: monaco.editor.ICodeEditor | undefined,
    beforeLinesMap?: {
      [lineNumber: number]: number
    },
    afterLinesMap?: {
      [lineNumber: number]: number
    },
  ) {
    this.pathPair = pathPair
    this.codeFragments = {
      before: beforeElements.filter(
        (element) => element.type === CodeElementType.CodeFragment,
      ),
      after: afterElements.filter(
        (element) => element.type === CodeElementType.CodeFragment,
      ),
    }
    this.linesMap.before = beforeLinesMap
    this.linesMap.after = afterLinesMap

    originalViewer?.onDidChangeCursorSelection(
      debounce((e: monaco.editor.ICursorSelectionChangedEvent) => {
        this.updateEditingCodeFragment('before', e.selection)
      }, 1000),
    )
    modifiedViewer?.onDidChangeCursorSelection(
      debounce((e: monaco.editor.ICursorSelectionChangedEvent) => {
        this.updateEditingCodeFragment('after', e.selection)
      }, 1000),
    )
  }

  public update(
    pathPair: PathPair,
    beforeElements: CodeElement[],
    afterElements: CodeElement[],
    beforeLinesMap?: {
      [lineNumber: number]: number
    },
    afterLinesMap?: {
      [lineNumber: number]: number
    },
  ) {
    this.pathPair = pathPair
    this.codeFragments.before = beforeElements.filter(
      (element) => element.type === CodeElementType.CodeFragment,
    )
    this.codeFragments.after = afterElements.filter(
      (element) => element.type === CodeElementType.CodeFragment,
    )
    this.linesMap.before = beforeLinesMap
    this.linesMap.after = afterLinesMap
  }

  private async updateEditingCodeFragment(
    category: DiffCategory,
    range: monaco.Range,
  ) {
    const inverseRange = this.mapRangeInverse(range, category)
    if (!inverseRange) return
    range = inverseRange
    if (
      range.startLineNumber === range.endLineNumber &&
      range.startColumn === range.endColumn
    )
      return

    const path = this.pathPair[category]
    if (path === undefined) return

    const element = this.codeFragments[category].find((e) =>
      asMonacoRange(e.location?.range).containsRange(range),
    ) ?? {
      type: CodeElementType.CodeFragment,
      location: {
        path,
        range: asRange(range),
      },
    }

    const metadata = useParameter().editingElement.value[category]
    const { annotationId, snapshotId, changeId } =
      useAnnotation().currentIds.value
    if (!metadata || !annotationId || !snapshotId || !changeId) return
    if (metadata.type !== CodeElementType.CodeFragment) return

    const nextElement = cloneDeep(element)
    nextElement.location!.range = asRange(range)

    useAnnotation().updateChange(
      (
        await apis.parameters.updateParameterValue(
          annotationId,
          snapshotId,
          changeId,
          category,
          metadata.key,
          metadata.index,
          {
            element: nextElement,
          },
        )
      ).data,
    )
    useParameter().updateEditingElement(category, undefined)
  }

  private mapRangeInverse(range: monaco.Range, category: DiffCategory) {
    let { startLineNumber, endLineNumber } = range
    const map = this.linesMap[category]
    if (map) {
      const oldStartLine = Object.entries(map).find(
        ([, newLineNumber]) => newLineNumber === startLineNumber,
      )
      if (!oldStartLine) return undefined
      startLineNumber = Number.parseInt(oldStartLine[0])

      const oldEndLine = Object.entries(map).find(
        ([, newLineNumber]) => newLineNumber === endLineNumber,
      )
      if (!oldEndLine) return undefined
      endLineNumber = Number.parseInt(oldEndLine[0])
    }
    return new monaco.Range(
      startLineNumber,
      range.startColumn,
      endLineNumber,
      range.endColumn,
    )
  }
}
