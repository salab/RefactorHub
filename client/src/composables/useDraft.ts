import { DiffCategory, FileMetadata, ElementMetadata } from 'refactorhub'
import apis, {
  CommitDetail,
  RefactoringDraft,
  RefactoringType,
  FileContent,
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
  const displayedFile = useState<{
    [category in DiffCategory]?: FileMetadata
  }>('displayedFile', () => ({
    before: undefined,
    after: undefined,
  }))
  const editingElement = useState<{
    [category in DiffCategory]?: ElementMetadata
  }>('editingElement', () => ({
    before: undefined,
    after: undefined,
  }))

  const initStates = async (draftId: number) => {
    // TODO: temporary fix
    draft.value = undefined
    commit.value = undefined
    displayedFile.value.before = undefined
    displayedFile.value.after = undefined
    editingElement.value.before = undefined
    editingElement.value.after = undefined

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

  return {
    draft,
    commit,
    refactoringTypes,
    elementTypes,
    fileContentCache,
    displayedFile,
    editingElement,
    initStates,
    getFileContent,
  }
}
