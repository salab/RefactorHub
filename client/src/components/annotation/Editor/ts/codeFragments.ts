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

  public initCodeFragmentCursor() {
    this.cursors.before.length = 0
    this.cursors.after.length = 0
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
    const listeners: monaco.IDisposable[] = []
    const cursor = {
      setup: () => {
        if (listeners.length === 0) {
          listeners.push(
            editor.onDidChangeCursorSelection(
              debounce((e) => {
                this.updateEditingCodeFragment(category, e.selection, element)
              }, 1000),
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

  private async updateEditingCodeFragment(
    category: DiffCategory,
    range: monaco.Range,
    element: CodeElement,
  ) {
    if (
      range.startLineNumber === range.endLineNumber &&
      range.startColumn === range.endColumn
    )
      return

    if (!asMonacoRange(element.location?.range).containsRange(range)) return

    const metadata = useParameter().editingElement.value[category]
    const { annotationId, snapshotId, changeId } =
      useAnnotation().currentIds.value
    if (!metadata || !annotationId || !snapshotId || !changeId) return

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
}
