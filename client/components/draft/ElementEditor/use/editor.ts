import { DiffCategory } from 'refactorhub'
import * as monaco from 'monaco-editor'
import { clearElementWidgets } from './elementWidget'

async function setFileContentOnEditor(
  category: DiffCategory,
  owner: string,
  repository: string,
  sha: string,
  path: string
) {
  const uri = this.$editor.getCommitUri(owner, repository, sha, path)
  const text = await this.$accessor.draft.getTextModel({
    owner,
    repository,
    sha,
    path,
    uri: uri.toString(),
  })
  const model =
    monaco.editor.getModel(uri) ||
    monaco.editor.createModel(text.value, text.language, uri)

  if (diff === 'before') {
    this.diffEditor.setModel({
      original: model,
      modified:
        this.diffEditor.getModifiedEditor().getModel() ||
        monaco.editor.createModel('', 'text/plain'),
    })
  } else if (diff === 'after') {
    this.diffEditor.setModel({
      original:
        this.diffEditor.getOriginalEditor().getModel() ||
        monaco.editor.createModel('', 'text/plain'),
      modified: model,
    })
  }

  const editor = getEditor(category)

  clearElementWidgets(category, editor)

  

  text.elements.forEach((element, i) => {
    if (element.type === 'Statements') {
      this.statements[diff].push(element)
    } else {
      const widget = this.$editor.createElementWidget(
        diff,
        editor,
        element,
        i,
        async () => {
          const draft = this.$accessor.draft.draft
          const item = this.$accessor.draft.element[diff]
          if (draft && item) {
            const key = item[0]
            this.$accessor.draft.setDraft(
              await this.$client.updateElement(draft.id, diff, key, element)
            )
            this.deleteElementDecoration(diff, key)
            this.setElementDecoration(diff, key, element)
            this.$accessor.draft.setElement({ diff })
          }
        }
      )
      editor.addContentWidget(widget)
      this.widgets[diff].push(widget)
    }
  })

  this.pending--
}
