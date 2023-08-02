import * as monaco from 'monaco-editor'
import { cloneDeep, debounce } from 'lodash-es'
import { DiffCategory } from 'refactorhub'
import { asRange, asMonacoRange } from '@/components/common/editor/utils/range'
import apis, { CodeElement } from '@/apis'

interface CodeFragmentCursor {
  setup(): void
  dispose(): void
}

const cursors: {
  [category in DiffCategory]: CodeFragmentCursor[]
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

export function initCodeFragmentCursor() {
  cursors.before.length = 0
  cursors.after.length = 0
  fragments.before.length = 0
  fragments.after.length = 0
}

export function setupCodeFragmentCursor(category: DiffCategory) {
  cursors[category].forEach((cursor) => cursor.setup())
}

export function disposeCodeFragmentCursor(category: DiffCategory) {
  cursors[category].forEach((cursor) => cursor.dispose())
}

export function clearCodeFragmentCursors(category: DiffCategory) {
  cursors[category].forEach((cursor) => cursor.dispose())
  cursors[category].length = 0
}

export function prepareCodeFragmentCursor(
  category: DiffCategory,
  element: CodeElement,
  editor: monaco.editor.ICodeEditor,
) {
  fragments[category].push(element)

  const listeners: monaco.IDisposable[] = []
  const cursor = {
    setup: () => {
      if (listeners.length === 0) {
        listeners.push(
          editor.onDidChangeCursorSelection(
            debounce((e) => {
              updateEditingCodeFragment(category, e.selection)
            }, 500),
          ),
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

export async function updateEditingCodeFragment(
  category: DiffCategory,
  range: monaco.Range,
) {
  if (
    range.startLineNumber === range.endLineNumber &&
    range.startColumn === range.endColumn
  )
    return

  const element = fragments[category].find((it) =>
    asMonacoRange(it.location?.range).containsRange(range),
  )
  if (!element) return

  const draft = useDraft().draft.value
  const metadata = useDraft().editingElement.value[category]
  if (!draft || !metadata) return

  const nextElement = cloneDeep(element)
  nextElement.location!.range = asRange(range)

  useDraft().draft.value = (
    await apis.drafts.updateCodeElementValue(
      draft.id,
      category,
      metadata.key,
      metadata.index,
      {
        element: nextElement,
      },
    )
  ).data
}
