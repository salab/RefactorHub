import * as monaco from 'monaco-editor'
import { cloneDeep, debounce } from 'lodash-es'
import { DiffCategory } from 'refactorhub'
import { asRange, asMonacoRange } from '@/components/common/editor/utils/range'
import apis, { CodeElement } from '@/apis'

interface CodeFragmentCursor {
  setup(): void
  dispose(): void
}

export class CodeFragmentManager {
  private readonly cursors: {
    [category in DiffCategory]: CodeFragmentCursor[]
  } = {
    before: [],
    after: [],
  }

  private readonly fragments: {
    [category in DiffCategory]: CodeElement[]
  } = {
    before: [],
    after: [],
  }

  public initCodeFragmentCursor() {
    this.cursors.before.length = 0
    this.cursors.after.length = 0
    this.fragments.before.length = 0
    this.fragments.after.length = 0
  }

  public setupCodeFragmentCursor(category: DiffCategory) {
    this.cursors[category].forEach((cursor) => cursor.setup())
  }

  public disposeCodeFragmentCursor(category: DiffCategory) {
    this.cursors[category].forEach((cursor) => cursor.dispose())
  }

  public clearCodeFragmentCursors(category: DiffCategory) {
    this.cursors[category].forEach((cursor) => cursor.dispose())
    this.cursors[category].length = 0
  }

  public prepareCodeFragmentCursor(
    category: DiffCategory,
    element: CodeElement,
    editor: monaco.editor.ICodeEditor,
  ) {
    this.fragments[category].push(element)

    const listeners: monaco.IDisposable[] = []
    const cursor = {
      setup: () => {
        if (listeners.length === 0) {
          listeners.push(
            editor.onDidChangeCursorSelection(
              debounce((e) => {
                this.updateEditingCodeFragment(category, e.selection)
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
    this.cursors[category].push(cursor)
  }

  public async updateEditingCodeFragment(
    category: DiffCategory,
    range: monaco.Range,
  ) {
    if (
      range.startLineNumber === range.endLineNumber &&
      range.startColumn === range.endColumn
    )
      return

    const element = this.fragments[category].find((it) =>
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
}
