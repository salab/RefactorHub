import * as monaco from 'monaco-editor'
import { DiffCategory, ElementMetadata } from 'refactorhub'
import { asMonacoRange } from '@/components/common/editor/utils/range'
import { Location } from '@/apis'
import { PathPair } from 'composables/useAnnotation'

/** Dependencies: beforePath, afterPath, linesMap */
export class ElementDecorationManager {
  private readonly viewer: {
    [category in DiffCategory]?: monaco.editor.ICodeEditor
  }

  private readonly decorationsCollection: {
    [category in DiffCategory]?: monaco.editor.IEditorDecorationsCollection
  } = {
    before: undefined,
    after: undefined,
  }

  private readonly linesMap: {
    [category in DiffCategory]?: {
      [lineNumber: number]: number
    }
  } = {
    before: undefined,
    after: undefined,
  }

  private pathPair: PathPair

  public constructor(
    pathPair: PathPair,
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
    this.viewer = {
      before: originalViewer,
      after: modifiedViewer,
    }
    this.linesMap.before = beforeLinesMap
    this.linesMap.after = afterLinesMap

    this.updateDecoration('before', useParameter().hoveredElement.value.before)
    this.updateDecoration('after', useParameter().hoveredElement.value.after)
    watch(
      () => useParameter().hoveredElement.value.before,
      (newElementMetaData) =>
        this.updateDecoration('before', newElementMetaData),
    )
    watch(
      () => useParameter().hoveredElement.value.after,
      (newElementMetaData) =>
        this.updateDecoration('after', newElementMetaData),
    )
  }

  public update(
    pathPair: PathPair,
    beforeLinesMap?: {
      [lineNumber: number]: number
    },
    afterLinesMap?: {
      [lineNumber: number]: number
    },
  ) {
    this.pathPair = pathPair
    this.linesMap.before = beforeLinesMap
    this.linesMap.after = afterLinesMap
    this.updateDecoration('before', useParameter().hoveredElement.value.before)
    this.updateDecoration('after', useParameter().hoveredElement.value.after)
  }

  private updateDecoration(
    category: DiffCategory,
    elementMetaData: ElementMetadata | undefined,
  ) {
    this.decorationsCollection[category]?.clear()
    const viewer = this.viewer[category]
    if (!elementMetaData || !viewer) return
    const location =
      useAnnotation().currentChange.value?.parameterData[category][
        elementMetaData.key
      ].elements[elementMetaData.index].location
    if (!location || location.path !== this.pathPair[category]) return
    this.decorationsCollection[category] = viewer.createDecorationsCollection([
      this.createElementDecoration(location, category),
    ])
  }

  private createElementDecoration(
    location: Location,
    category: DiffCategory,
  ): monaco.editor.IModelDeltaDecoration {
    return {
      range: this.mapRange(asMonacoRange(location.range), category),
      options: {
        className: 'element-decoration',
      },
    }
  }

  private mapRange(range: monaco.Range, category: DiffCategory) {
    let { startLineNumber, endLineNumber } = range
    const map = this.linesMap[category]
    if (map) {
      startLineNumber = map[startLineNumber]
      endLineNumber = map[endLineNumber]
    }
    return new monaco.Range(
      startLineNumber,
      range.startColumn,
      endLineNumber,
      range.endColumn,
    )
  }
}
