import * as monaco from 'monaco-editor'
import { cloneDeep } from 'lodash'
import cryptoRandomString from 'crypto-random-string'
import { DiffCategory, Element } from 'refactorhub'
import {
  setElementDecorationOnEditor,
  deleteElementDecoration,
} from './decorations'
import {
  asMonacoRange,
  asRange,
  getRangeWidthOnEditor,
  getRangeHeightOnEditor,
} from '@/components/common/editor/use/range'
import { Client } from '@/plugins/client'
import { accessorType } from '@/store'

interface ElementWidget extends monaco.editor.IContentWidget {
  type: string
}

export class ElementWidgetManager {
  private $accessor: typeof accessorType
  private $client: Client

  constructor($accessor: typeof accessorType, $client: Client) {
    this.$accessor = $accessor
    this.$client = $client
  }

  private widgets: {
    [category in DiffCategory]: ElementWidget[]
  } = {
    before: [],
    after: [],
  }

  private fragments: {
    [category in DiffCategory]: Element[]
  } = {
    before: [],
    after: [],
  }

  /**
   * show element widgets with specified type & hide others
   */
  showElementWidgetsWithType(category: DiffCategory, type: string) {
    this.widgets[category].forEach((widget) => {
      if (widget.type === type) widget.getDomNode().style.display = 'block'
      else widget.getDomNode().style.display = 'none'
    })
  }

  hideElementWidgets(category: DiffCategory) {
    this.widgets[category].forEach((widget) => {
      widget.getDomNode().style.display = 'none'
    })
  }

  clearElementWidgetsOnEditor(
    category: DiffCategory,
    editor: monaco.editor.ICodeEditor
  ) {
    this.widgets[category].forEach((widget) => {
      editor.removeContentWidget(widget)
    })
  }

  setElementWidgetOnEditor(
    category: DiffCategory,
    element: Element,
    editor: monaco.editor.ICodeEditor
  ) {
    if (element.type === 'CodeFragments') {
      this.fragments[category].push(element)
    } else {
      const widget = this.createElementWidget(element, editor, () =>
        this.updateEditingElement(category, element, editor)
      )
      editor.addContentWidget(widget)
      this.widgets[category].push(widget)
    }
  }

  createElementWidget(
    element: Element,
    editor: monaco.editor.ICodeEditor,
    onClick: () => void
  ): ElementWidget {
    const range = asMonacoRange(element.location.range)
    const id = cryptoRandomString({ length: 10 })
    const div = document.createElement('div')
    div.classList.add('element-widget', `element-widget-${element.type}`)
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

  async updateEditingElement(
    category: DiffCategory,
    element: Element,
    editor: monaco.editor.ICodeEditor
  ) {
    const draft = this.$accessor.draft.draft
    const metadata = this.$accessor.draft.editingElementMetadata[category]
    if (!draft || !metadata) return

    this.$accessor.draft.setDraft(
      await this.$client.updateElement(
        draft.id,
        category,
        metadata.key,
        metadata.index,
        element
      )
    )

    deleteElementDecoration(category, metadata.key, metadata.index)
    setElementDecorationOnEditor(
      category,
      metadata.key,
      metadata.index,
      element,
      editor
    )

    this.$accessor.draft.setEditingElementMetadata({ category })
  }

  async updateEditingCodeFragments(
    category: DiffCategory,
    range: monaco.Range,
    editor: monaco.editor.ICodeEditor
  ) {
    const element = this.fragments[category].find((it) =>
      asMonacoRange(it.location.range).containsRange(range)
    )
    if (!element) return

    const draft = this.$accessor.draft.draft
    const metadata = this.$accessor.draft.editingElementMetadata[category]
    if (!draft || !metadata) return

    const nextElement = cloneDeep(element)
    nextElement.location.range = asRange(range)

    this.$accessor.draft.setDraft(
      await this.$client.updateElement(
        draft.id,
        category,
        metadata.key,
        metadata.index,
        nextElement
      )
    )

    deleteElementDecoration(category, metadata.key, metadata.index)
    setElementDecorationOnEditor(
      category,
      metadata.key,
      metadata.index,
      nextElement,
      editor
    )

    this.$accessor.draft.setEditingElementMetadata({ category })
  }
}
