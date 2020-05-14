import * as monaco from 'monaco-editor'
import { DiffCategory, FileMetadata, CommitInfo } from 'refactorhub'
import { setElementDecorationOnEditor } from './decorations'
import {
  setElementWidgetOnEditor,
  clearElementWidgetsOnEditor,
} from './elementWidgets'
import { accessorType } from '@/store'
import { Client } from '@/plugins/client'

export async function changeDisplayedFileOnDiffEditor(
  category: DiffCategory,
  metadata: FileMetadata,
  diffEditor: monaco.editor.IDiffEditor,
  $accessor: typeof accessorType,
  $client: Client
) {
  await setTextModelOnDiffEditor(category, metadata, diffEditor, $accessor)
  await setupElementDecorationsOnDiffEditor(
    category,
    metadata,
    diffEditor,
    $accessor
  )
  await setupElementWidgetsOnDiffEditor(
    category,
    metadata,
    diffEditor,
    $accessor,
    $client
  )
}

async function setTextModelOnDiffEditor(
  category: DiffCategory,
  metadata: FileMetadata,
  diffEditor: monaco.editor.IDiffEditor,
  $accessor: typeof accessorType
) {
  const textModel = await getTextModelOfFile(category, metadata, $accessor)
  if (!textModel) return
  if (category === 'before') {
    diffEditor.setModel({
      original: textModel,
      modified:
        diffEditor.getModifiedEditor().getModel() ||
        monaco.editor.createModel('', 'text/plain'),
    })
  } else if (category === 'after') {
    diffEditor.setModel({
      original:
        diffEditor.getOriginalEditor().getModel() ||
        monaco.editor.createModel('', 'text/plain'),
      modified: textModel,
    })
  }
}

async function getTextModelOfFile(
  category: DiffCategory,
  metadata: FileMetadata,
  $accessor: typeof accessorType
) {
  const commitInfo = $accessor.draft.commitInfo
  if (!commitInfo) {
    console.log('commitInfo is not loaded')
    return
  }

  const sha = getCommitSHA(category, commitInfo)
  const path = getCommitFileName(category, commitInfo, metadata)
  const content = await $accessor.draft.getFileContent({
    owner: commitInfo.owner,
    repository: commitInfo.repository,
    sha,
    path,
    uri: getCommitFileUri(commitInfo.owner, commitInfo.repository, sha, path),
  })
  return (
    monaco.editor.getModel(monaco.Uri.parse(content.uri)) ||
    monaco.editor.createModel(
      content.value,
      content.language,
      monaco.Uri.parse(content.uri)
    )
  )
}

function setupElementDecorationsOnDiffEditor(
  category: DiffCategory,
  metadata: FileMetadata,
  diffEditor: monaco.editor.IDiffEditor,
  $accessor: typeof accessorType
) {
  const draft = $accessor.draft.draft
  const commitInfo = $accessor.draft.commitInfo
  if (!draft || !commitInfo) {
    console.log('draft or commitInfo is not loaded')
    return
  }

  const path = getCommitFileName(category, commitInfo, metadata)
  const editor = getEditor(category, diffEditor)
  Object.entries(draft.data[category]).forEach(([key, data]) => {
    data.elements.forEach((element, index) => {
      if (path === element.location.path) {
        setElementDecorationOnEditor(category, key, index, element, editor)
      }
    })
  })
}

async function setupElementWidgetsOnDiffEditor(
  category: DiffCategory,
  metadata: FileMetadata,
  diffEditor: monaco.editor.IDiffEditor,
  $accessor: typeof accessorType,
  $client: Client
) {
  const commitInfo = $accessor.draft.commitInfo
  if (!commitInfo) {
    console.log('commitInfo is not loaded')
    return
  }

  const sha = getCommitSHA(category, commitInfo)
  const path = getCommitFileName(category, commitInfo, metadata)
  const content = await $accessor.draft.getFileContent({
    owner: commitInfo.owner,
    repository: commitInfo.repository,
    sha,
    path,
    uri: getCommitFileUri(commitInfo.owner, commitInfo.repository, sha, path),
  })
  const editor = getEditor(category, diffEditor)

  clearElementWidgetsOnEditor(category, editor)
  content.elements.forEach((element) => {
    setElementWidgetOnEditor(category, element, editor, $accessor, $client)
  })
}

function getEditor(
  category: DiffCategory,
  diffEditor: monaco.editor.IDiffEditor
) {
  return category === 'before'
    ? diffEditor.getOriginalEditor()
    : diffEditor.getModifiedEditor()
}

function getCommitSHA(category: DiffCategory, commitInfo: CommitInfo) {
  return category === 'before' ? commitInfo.parent : commitInfo.sha
}

function getCommitFileName(
  category: DiffCategory,
  commitInfo: CommitInfo,
  metadata: FileMetadata
) {
  const file = commitInfo.files[metadata.index]
  return category === 'before' ? file.previousName : file.name
}

function getCommitFileUri(
  owner: string,
  repository: string,
  sha: string,
  path: string
) {
  return `https://github.com/${owner}/${repository}/blob/${sha}/${path}`
}