import { getterTree, mutationTree, actionTree } from 'nuxt-typed-vuex'
import apis, {
  CommitDetail,
  RefactoringDraft,
  RefactoringType,
  FileContent,
} from '@/apis'
import { DiffCategory, FileMetadata, ElementMetadata } from 'refactorhub'

export const state = (): {
  draft?: RefactoringDraft
  commit?: CommitDetail
  refactoringTypes: RefactoringType[]
  elementTypes: string[]
  fileContentCache: Map<string, FileContent>
  displayedFile: {
    [category in DiffCategory]?: FileMetadata
  }
  editingElement: {
    [category in DiffCategory]?: ElementMetadata
  }
} => ({
  draft: undefined,
  commit: undefined,
  refactoringTypes: [],
  elementTypes: [],
  fileContentCache: new Map(),
  displayedFile: {
    before: undefined,
    after: undefined,
  },
  editingElement: {
    before: undefined,
    after: undefined,
  },
})

export const getters = getterTree(state, {})

export const mutations = mutationTree(state, {
  setDraft: (state, draft?: RefactoringDraft) => {
    state.draft = draft
  },

  setCommit: (state, commit?: CommitDetail) => {
    state.commit = commit
  },

  setRefactoringTypes: (state, types: RefactoringType[]) => {
    state.refactoringTypes = types
  },

  setCodeElementTypes: (state, types: string[]) => {
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

  setDisplayedFile: (
    state,
    {
      category,
      file,
    }: {
      category: DiffCategory
      file?: FileMetadata
    }
  ) => {
    state.displayedFile[category] = file
  },

  setEditingElement: (
    state,
    {
      category,
      element,
    }: {
      category: DiffCategory
      element?: ElementMetadata
    }
  ) => {
    state.editingElement[category] = element
  },
})

export const actions = actionTree(
  { state, getters, mutations },
  {
    async initStates({ commit }, id: number) {
      // TODO: temporary fix
      await commit('setDraft', undefined)
      await commit('setCommit', undefined)
      await commit('setDisplayedFile', { category: 'before' })
      await commit('setDisplayedFile', { category: 'after' })
      await commit('setEditingElement', { category: 'before' })
      await commit('setEditingElement', { category: 'after' })

      const draft = (await apis.drafts.getRefactoringDraft(id)).data
      await commit('setDraft', draft)
      await commit(
        'setCommit',
        (
          await apis.commits.getCommitDetail(
            draft.commit.owner,
            draft.commit.repository,
            draft.commit.sha
          )
        ).data
      )
      await commit(
        'setRefactoringTypes',
        (await apis.refactoringTypes.getAllRefactoringTypes()).data
      )
      await commit(
        'setCodeElementTypes',
        (await apis.elements.getCodeElementTypes()).data
      )
    },

    async getFileContent(
      { commit, state },
      {
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
      }
    ) {
      if (state.fileContentCache.has(uri)) {
        return state.fileContentCache.get(uri)!!
      }
      const content = (
        await apis.annotator.getFileContent(
          owner,
          repository,
          sha,
          category,
          path
        )
      ).data
      await commit('cacheFileContent', {
        uri,
        content,
      })
      return content
    },
  }
)
