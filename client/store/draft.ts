import { getterTree, mutationTree, actionTree } from 'nuxt-typed-vuex'
import {
  Draft,
  CommitInfo,
  RefactoringType,
  FileContent,
  DiffCategory,
  FileMetadata,
  ElementMetadata,
} from 'refactorhub'

export const state = (): {
  /** draft of refactoring instance displayed on /draft/:id */
  draft: Draft | null

  /** commit displayed on /draft/:id */
  commitInfo?: CommitInfo

  /** available refactoring types */
  refactoringTypes: RefactoringType[]

  /** available element types */
  elementTypes: string[]

  /** cached FileContents */
  fileContentCache: Map<string, FileContent>

  /** metadata of file displayed on editor */
  displayedFileMetadata: {
    [category in DiffCategory]?: FileMetadata
  }

  /** metadata of element editing on editor */
  editingElementMetadata: {
    [category in DiffCategory]?: ElementMetadata
  }
} => ({
  draft: null,
  commitInfo: undefined,
  refactoringTypes: [],
  elementTypes: [],
  fileContentCache: new Map(),
  displayedFileMetadata: {
    before: undefined,
    after: undefined,
  },
  editingElementMetadata: {
    before: undefined,
    after: undefined,
  },
})

export const getters = getterTree(state, {})

export const mutations = mutationTree(state, {
  setDraft: (state, draft: Draft) => {
    state.draft = draft
  },

  setCommitInfo: (state, info: CommitInfo) => {
    state.commitInfo = info
  },

  setRefactoringTypes: (state, types: RefactoringType[]) => {
    state.refactoringTypes = types
  },

  setElementTypes: (state, types: string[]) => {
    state.elementTypes = types
  },

  cacheFileContent: (
    state,
    {
      uri,
      content,
    }: {
      uri: string
      content: FileContent
    }
  ) => {
    state.fileContentCache.set(uri, content)
  },

  setDisplayedFileMetadata: (
    state,
    {
      category,
      metadata,
    }: {
      category: DiffCategory
      metadata?: FileMetadata
    }
  ) => {
    state.displayedFileMetadata[category] = metadata
  },

  setEditingElementMetadata: (
    state,
    {
      category,
      metadata,
    }: {
      category: DiffCategory
      metadata?: ElementMetadata
    }
  ) => {
    state.editingElementMetadata[category] = metadata
  },
})

export const actions = actionTree(
  { state, getters, mutations },
  {
    async initDraftStates({ commit }, id: number) {
      const draft = await this.$client.getDraft(id)
      await commit('setDraft', draft)
      await commit(
        'setCommitInfo',
        await this.$client.getCommitInfo(draft.commit.sha)
      )
      await commit(
        'setRefactoringTypes',
        await this.$client.getRefactoringTypes()
      )
      await commit('setElementTypes', await this.$client.getElementTypes())
      await commit('setDisplayedFileMetadata', {
        category: 'before',
        metadata: { index: 0 },
      })
      await commit('setDisplayedFileMetadata', {
        category: 'after',
        metadata: { index: 0 },
      })
    },

    async getFileContent(
      { commit, state },
      {
        owner,
        repository,
        sha,
        path,
        uri,
      }: {
        owner: string
        repository: string
        sha: string
        path: string
        uri: string
      }
    ) {
      if (state.fileContentCache.has(uri)) {
        return state.fileContentCache.get(uri)!!
      }
      const content = await this.$client.getFileContent(
        owner,
        repository,
        sha,
        path
      )
      await commit('cacheFileContent', {
        uri,
        content,
      })
      return content
    },
  }
)
