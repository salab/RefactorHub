import * as monaco from 'monaco-editor'
import cryptoRandomString from 'crypto-random-string'
import { DiffCategory, ElementMetadata } from 'refactorhub'
import {
  asMonacoRange,
  getRangeWidthOnEditor,
  getRangeHeightOnEditor,
} from '@/components/common/editor/utils/range'
import apis, { CodeElement, CodeElementType } from '@/apis'

interface ElementWidget extends monaco.editor.IContentWidget {
  type: string
}

/** Dependencies: beforeElements, afterElements */
export class ElementWidgetManager {
  private readonly viewer: {
    [category in DiffCategory]?: monaco.editor.ICodeEditor
  }

  private readonly codeElements: {
    [category in DiffCategory]: CodeElement[]
  }

  private readonly widgets: {
    [category in DiffCategory]: ElementWidget[]
  } = {
    before: [],
    after: [],
  }

  public constructor(
    beforeElements: CodeElement[],
    afterElements: CodeElement[],
    originalViewer: monaco.editor.ICodeEditor | undefined,
    modifiedViewer: monaco.editor.ICodeEditor | undefined,
  ) {
    this.codeElements = {
      before: beforeElements.filter(
        (element) => element.type !== CodeElementType.CodeFragment,
      ),
      after: afterElements.filter(
        (element) => element.type !== CodeElementType.CodeFragment,
      ),
    }
    this.viewer = {
      before: originalViewer,
      after: modifiedViewer,
    }
    this.resetElementWidgets()

    watch(
      () => useParameter().editingElement.value.before,
      (newElementMetaData) =>
        this.visualizeElementWidgets('before', newElementMetaData),
    )
    watch(
      () => useParameter().editingElement.value.after,
      (newElementMetaData) =>
        this.visualizeElementWidgets('after', newElementMetaData),
    )
  }

  public update(beforeElements: CodeElement[], afterElements: CodeElement[]) {
    this.codeElements.before = beforeElements.filter(
      (element) => element.type !== CodeElementType.CodeFragment,
    )
    this.codeElements.after = afterElements.filter(
      (element) => element.type !== CodeElementType.CodeFragment,
    )
    this.resetElementWidgets()
  }

  private resetElementWidgets() {
    if (this.viewer.before) {
      this.clearElementWidgets('before', this.viewer.before)
      this.createElementWidgets('before', this.viewer.before)
      this.visualizeElementWidgets(
        'before',
        useParameter().editingElement.value.before,
      )
    }
    if (this.viewer.after) {
      this.clearElementWidgets('after', this.viewer.after)
      this.createElementWidgets('after', this.viewer.after)
      this.visualizeElementWidgets(
        'after',
        useParameter().editingElement.value.after,
      )
    }
  }

  /**
   * show element widgets with specified type & hide others
   */
  private visualizeElementWidgets(
    category: DiffCategory,
    editingElementMetaData: ElementMetadata | undefined,
  ) {
    this.widgets[category].forEach((widget) => {
      if (widget.type === editingElementMetaData?.type)
        widget.getDomNode().style.display = 'block'
      else widget.getDomNode().style.display = 'none'
    })
  }

  private clearElementWidgets(
    category: DiffCategory,
    viewer: monaco.editor.ICodeEditor,
  ) {
    this.widgets[category].forEach((widget) => {
      viewer.removeContentWidget(widget)
    })
    this.widgets[category].length = 0
  }

  private createElementWidgets(
    category: DiffCategory,
    viewer: monaco.editor.ICodeEditor,
  ) {
    this.codeElements[category].forEach((element) => {
      const widget = this.createElementWidget(element, viewer, () =>
        this.updateParameterValue(category, element),
      )
      viewer.addContentWidget(widget)
      this.widgets[category].push(widget)
    })
  }

  private createElementWidget(
    element: CodeElement,
    editor: monaco.editor.ICodeEditor,
    onClick: () => void,
  ): ElementWidget {
    const range = asMonacoRange(element.location?.range)
    const id = cryptoRandomString({ length: 10 })
    const div = document.createElement('div')
    div.classList.add('element-widget')
    div.style.width = `${getRangeWidthOnEditor(range, editor)}px`
    div.style.height = `${getRangeHeightOnEditor(range, editor)}px`
    div.style.minWidth = div.style.width
    div.style.display = 'none'
    div.addEventListener('click', onClick)

    // re-compute height after diff computed
    setTimeout(() => {
      try {
        div.style.height = `${getRangeHeightOnEditor(range, editor)}px`
      } catch (e) {}
    }, 1000)

    return {
      type: `${element.type}`,
      getId: () => id,
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

  private async updateParameterValue(
    category: DiffCategory,
    element: CodeElement,
  ) {
    const metadata = useParameter().editingElement.value[category]
    const { annotationId, snapshotId, changeId } =
      useAnnotation().currentIds.value
    if (!metadata || !annotationId || !snapshotId || !changeId) return

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
            element,
          },
        )
      ).data,
    )
    useParameter().updateEditingElement(category, undefined)
  }
}
