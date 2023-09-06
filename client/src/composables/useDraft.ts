import * as monaco from 'monaco-editor'
import { DiffCategory, ElementMetadata } from 'refactorhub'
import apis, {
  CommitDetail,
  RefactoringDraft,
  RefactoringType,
  FileContent,
  CommitFile,
} from '@/apis'
import { useState } from '#imports'

type URI = string

export const useDraft = () => {
  const draft = useState<RefactoringDraft | undefined>('draft', () => undefined)
  const commit = useState<CommitDetail | undefined>('commit', () => undefined)
  const refactoringTypes = useState<RefactoringType[]>(
    'refactoringTypes',
    () => [],
  )
  const elementTypes = useState<string[]>('elementTypes', () => [])
  const fileContentCache = useState<Map<URI, FileContent>>(
    'fileContentCache',
    () => new Map(),
  )
  const editingElement = useState<{
    [category in DiffCategory]?: ElementMetadata
  }>('editingElement', () => ({
    before: undefined,
    after: undefined,
  }))

  function initialize() {
    draft.value = undefined
    commit.value = undefined
    refactoringTypes.value = []
    elementTypes.value = []
    editingElement.value.before = undefined
    editingElement.value.after = undefined
  }

  async function setup(draftId: number): Promise<CommitDetail> {
    useLoader().setLoadingText('loading draft')
    draft.value = (await apis.drafts.getRefactoringDraft(draftId)).data
    const commitDetail = (
      await apis.commits.getCommitDetail(
        draft.value.commit.owner,
        draft.value.commit.repository,
        draft.value.commit.sha,
      )
    ).data
    commit.value = commitDetail
    refactoringTypes.value = (
      await apis.refactoringTypes.getAllRefactoringTypes()
    ).data
    elementTypes.value = (await apis.elements.getCodeElementTypes()).data

    await cacheAllFileContent(commitDetail)
    return commitDetail
  }

  async function cacheAllFileContent(commit: CommitDetail) {
    const { owner, repository, sha, parent, files } = commit
    let cachedCount = 0
    useLoader().setLoadingText(
      `fetching changed file contents (${cachedCount} / ${files.length * 2})`,
    )
    function updateLoadingText() {
      cachedCount++
      useLoader().setLoadingText(
        `fetching changed file contents (${cachedCount} / ${files.length * 2})`,
      )
    }
    await Promise.all([
      ...files.map(async (file) => {
        await getFileContent({
          owner,
          repository,
          sha,
          category: 'before',
          path: file.previousName,
          uri: getCommitFileUri(owner, repository, parent, file.previousName),
        })
        updateLoadingText()
      }),
      ...files.map(async (file) => {
        await getFileContent({
          owner,
          repository,
          sha,
          category: 'after',
          path: file.name,
          uri: getCommitFileUri(owner, repository, sha, file.name),
        })
        updateLoadingText()
      }),
    ])
  }

  const getFileContent = async ({
    owner,
    repository,
    sha,
    category,
    path,
    uri,
  }: {
    owner: string
    repository: string
    sha: string
    category: DiffCategory
    path: string
    uri: string
  }) => {
    const cachedContent = fileContentCache.value.get(uri)
    if (cachedContent) return cachedContent
    const content = (
      await apis.annotator.getFileContent(
        owner,
        repository,
        sha,
        category,
        path,
      )
    ).data
    fileContentCache.value.set(uri, content)
    return content
  }

  function isExistingFile(category: DiffCategory, file: CommitFile) {
    return !(
      (category === 'before' && file.status === 'added') ||
      (category === 'after' && file.status === 'removed')
    )
  }
  function getCommitSHA(category: DiffCategory) {
    if (!commit.value) {
      throw new Error('commit is not loaded')
    }
    return category === 'before' ? commit.value.parent : commit.value.sha
  }
  function getCommitFileName(category: DiffCategory, file: CommitFile) {
    return category === 'before' ? file.previousName : file.name
  }
  function getCommitFileUri(
    owner: string,
    repository: string,
    sha: string,
    path: string,
  ) {
    return `https://github.com/${owner}/${repository}/blob/${sha}/${path}`
  }
  function getCommitFile(category: DiffCategory, path: string) {
    if (!commit.value) {
      logger.error('commit is not loaded')
      return undefined
    }
    return commit.value?.files.find(
      (file) => getCommitFileName(category, file) === path,
    )
  }
  async function getTextModelOfFile(category: DiffCategory, path: string) {
    const c = commit.value
    const file = getCommitFile(category, path)
    if (!c || !file || !isExistingFile(category, file))
      return monaco.editor.createModel('', 'text/plain')

    const sha = getCommitSHA(category)
    const content = await useDraft().getFileContent({
      owner: c.owner,
      repository: c.repository,
      sha: c.sha,
      category,
      path,
      uri: getCommitFileUri(c.owner, c.repository, sha, path),
    })
    return (
      monaco.editor.getModel(monaco.Uri.parse(content.uri)) ||
      monaco.editor.createModel(
        content.text,
        content.extension === 'java' ? 'java' : 'text/plain',
        monaco.Uri.parse(content.uri),
      )
    )
  }

  return {
    draft,
    commit,
    refactoringTypes,
    elementTypes,
    fileContentCache,
    editingElement,
    initialize,
    setup,
    getFileContent,
    getCommitFileUri,
    getTextModelOfFile,
  }
}
