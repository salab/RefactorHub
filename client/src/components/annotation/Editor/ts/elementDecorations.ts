import * as monaco from 'monaco-editor'
import { DiffCategory, ElementMetadata } from 'refactorhub'
import { asMonacoRange } from '@/components/common/editor/utils/range'
import { Location } from '@/apis'
import { PathPair } from 'composables/useAnnotation'

/** Dependencies: beforePath, afterPath */
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

  private pathPair: PathPair

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

  public update(pathPair: PathPair) {
    this.pathPair = pathPair
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
      this.createElementDecoration(location),
    ])
  }

  private createElementDecoration(
    location: Location,
  ): monaco.editor.IModelDeltaDecoration {
    return {
      range: asMonacoRange(location.range),
      options: {
        className: 'element-decoration',
      },
    }
  }
}
