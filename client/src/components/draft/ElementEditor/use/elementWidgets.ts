import * as monaco from 'monaco-editor'
import cryptoRandomString from 'crypto-random-string'
import { DiffCategory, Element } from 'refactorhub'
import {
  asMonacoRange,
  getRangeWidthOnEditor,
  getRangeHeightOnEditor,
} from '@/components/common/editor/use/range'
import { Client } from '@/plugins/client'
import { accessorType } from '@/store'
import {
  setElementDecorationOnEditor,
  deleteElementDecoration,
} from './elementDecorations'

interface ElementWidget extends monaco.editor.IContentWidget {
  type: string
}

const widgets: {
  [category in DiffCategory]: ElementWidget[]
} = {
  before: [],
  after: [],
}

export function initElementWidgets() {
  widgets.before.length = 0
  widgets.after.length = 0
}

/**
 * show element widgets with specified type & hide others
 */
export function showElementWidgetsWithType(
  category: DiffCategory,
  type: string
) {
  widgets[category].forEach((widget) => {
    if (widget.type === type) widget.getDomNode().style.display = 'block'
    else widget.getDomNode().style.display = 'none'
  })
}

export function hideElementWidgets(category: DiffCategory) {
  widgets[category].forEach((widget) => {
    widget.getDomNode().style.display = 'none'
  })
}

export function clearElementWidgetsOnEditor(
  category: DiffCategory,
  editor: monaco.editor.ICodeEditor
) {
  widgets[category].forEach((widget) => {
    editor.removeContentWidget(widget)
  })
  widgets[category].length = 0
}

export function setElementWidgetOnEditor(
  category: DiffCategory,
  element: Element,
  editor: monaco.editor.ICodeEditor,
  $accessor: typeof accessorType,
  $client: Client
) {
  const widget = createElementWidget(element, editor, () =>
    updateEditingElement(category, element, editor, $accessor, $client)
  )
  editor.addContentWidget(widget)
  widgets[category].push(widget)
}

function createElementWidget(
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

async function updateEditingElement(
  category: DiffCategory,
  element: Element,
  editor: monaco.editor.ICodeEditor,
  $accessor: typeof accessorType,
  $client: Client
) {
  const draft = $accessor.draft.draft
  const metadata = $accessor.draft.editingElementMetadata[category]
  if (!draft || !metadata) return

  $accessor.draft.setDraft(
    await $client.updateElementValue(
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

  $accessor.draft.setEditingElementMetadata({ category })
}
