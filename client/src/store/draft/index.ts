import { getterTree, mutationTree, actionTree } from 'nuxt-typed-vuex'
import apis, {
  CommitDetail,
  RefactoringDraft,
  RefactoringType,
  FileContent,
} from '@/apis'

export const state = (): {
  draft?: RefactoringDraft
  commit?: CommitDetail
  refactoringTypes: RefactoringType[]
  elementTypes: string[]
  fileContentCache: Map<string, FileContent>
} => ({
  draft: undefined,
  commit: undefined,
  refactoringTypes: [],
  elementTypes: [],
  fileContentCache: new Map(),
})

export const getters = getterTree(state, {})

export const mutations = mutationTree(state, {
  setDraft: (state, draft: RefactoringDraft) => {
    state.draft = draft
  },

  setCommit: (state, commit: CommitDetail) => {
    state.commit = commit
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
})

export const actions = actionTree(
  { state, getters, mutations },
  {
    async initStates({ commit }, id: number) {
      const draft = (await apis.drafts.getRefactoringDraft(id)).data
      await commit('setDraft', draft)
      await commit(
        'setCommit',
        (await apis.commits.getCommitDetail(draft.commit.sha)).data
      )
      await commit(
        'setRefactoringTypes',
        (await apis.refactoringTypes.getAllRefactoringTypes()).data
      )
      await commit(
        'setElementTypes',
        (await apis.elements.getElementTypes()).data
      )
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
      const content = (
        await apis.editor.getFileContent(owner, repository, sha, path)
      ).data
      await commit('cacheFileContent', {
        uri,
        content,
      })
      return content
    },
  }
)
