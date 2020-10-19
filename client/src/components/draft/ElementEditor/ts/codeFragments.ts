import * as monaco from 'monaco-editor'
import { cloneDeep, debounce } from 'lodash-es'
import { DiffCategory } from 'refactorhub'
import { asRange, asMonacoRange } from '@/components/common/editor/utils/range'
import { accessorType } from '@/store'
import apis, { CodeElement } from '@/apis'
import {
  deleteElementDecoration,
  setElementDecorationOnEditor,
} from './elementDecorations'

interface CodeFragmentsCursor {
  setup(): void
  dispose(): void
}

const cursors: {
  [category in DiffCategory]: CodeFragmentsCursor[]
} = {
  before: [],
  after: [],
}

const fragments: {
  [category in DiffCategory]: CodeElement[]
} = {
  before: [],
  after: [],
}

export function initCodeFragmentsCursor() {
  cursors.before.length = 0
  cursors.after.length = 0
  fragments.before.length = 0
  fragments.after.length = 0
}

export function setupCodeFragmentsCursor(category: DiffCategory) {
  cursors[category].forEach((cursor) => cursor.setup())
}

export function disposeCodeFragmentsCursor(category: DiffCategory) {
  cursors[category].forEach((cursor) => cursor.dispose())
}

export function clearCodeFragmentsCursors(category: DiffCategory) {
  cursors[category].forEach((cursor) => cursor.dispose())
  cursors[category].length = 0
}

export function prepareCodeFragmentsCursor(
  category: DiffCategory,
  element: CodeElement,
  editor: monaco.editor.ICodeEditor,
  $accessor: typeof accessorType
) {
  fragments[category].push(element)

  const listeners: monaco.IDisposable[] = []
  const cursor = {
    setup: () => {
      if (listeners.length === 0) {
        listeners.push(
          editor.onDidChangeCursorSelection(
            debounce((e) => {
              updateEditingCodeFragments(
                category,
                e.selection,
                editor,
                $accessor
              )
            }, 500)
          )
        )
      }
    },
    dispose: () => {
      listeners.forEach((listener) => listener.dispose())
      listeners.length = 0
    },
  }
  cursors[category].push(cursor)
}

export async function updateEditingCodeFragments(
  category: DiffCategory,
  range: monaco.Range,
  editor: monaco.editor.ICodeEditor,
  $accessor: typeof accessorType
) {
  if (
    range.startLineNumber === range.endLineNumber &&
    range.startColumn === range.endColumn
  )
    return

  const element = fragments[category].find((it) =>
    asMonacoRange(it.location?.range).containsRange(range)
  )
  if (!element) return

  const draft = $accessor.draft.draft
  const metadata = $accessor.draft.editingElement[category]
  if (!draft || !metadata) return

  const nextElement = cloneDeep(element)
  nextElement.location!.range = asRange(range)

  $accessor.draft.setDraft(
    (
      await apis.drafts.updateRefactoringDraftElementValue(
        draft.id,
        category,
        metadata.key,
        metadata.index,
        {
          element: nextElement,
        }
      )
    ).data
  )

  deleteElementDecoration(category, metadata.key, metadata.index)
  setElementDecorationOnEditor(
    category,
    metadata.key,
    metadata.index,
    nextElement,
    editor
  )
}
