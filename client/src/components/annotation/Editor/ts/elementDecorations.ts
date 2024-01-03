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

  private readonly elements: globalThis.ComputedRef<{
    before: ElementMetadata[]
    after: ElementMetadata[]
  }>

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

    this.elements = computed(() => {
      const { before: beforeElements, after: afterElements } =
        useParameter().autoHighlightedElements.value
      const elements = {
        before: [...beforeElements],
        after: [...afterElements],
      }
      const { before, after } = useParameter().hoveredElement.value
      if (before) elements.before.push(before)
      if (after) elements.after.push(after)
      return elements
    })

    this.updateDecoration('before', this.elements.value.before)
    this.updateDecoration('after', this.elements.value.after)
    watch(
      () => this.elements.value.before.length,
      () => this.updateDecoration('before', this.elements.value.before),
    )
    watch(
      () => this.elements.value.after.length,
      () => this.updateDecoration('after', this.elements.value.after),
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
    this.updateDecoration('before', this.elements.value.before)
    this.updateDecoration('after', this.elements.value.after)
  }

  private updateDecoration(
    category: DiffCategory,
    elements: ElementMetadata[],
  ) {
    this.decorationsCollection[category]?.clear()
    const viewer = this.viewer[category]
    if (!viewer) return
    const locations: Location[] = []
    for (const element of elements) {
      const location =
        useAnnotation().currentChange.value?.parameterData[category][
          element.key
        ].elements[element.index].location
      if (!location || location.path !== this.pathPair[category]) continue
      locations.push(location)
    }
    this.decorationsCollection[category] = viewer.createDecorationsCollection(
      locations.map((location) =>
        this.createElementDecoration(location, category),
      ),
    )
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
