import { getterTree, mutationTree, actionTree } from 'nuxt-typed-vuex'
import { Draft, CommitInfo, RefactoringType } from 'refactorhub'

export const state = (): {
  draft?: Draft
  commit?: CommitInfo
  refactoringTypes: RefactoringType[]
  elementTypes: string[]
} => ({
  draft: undefined,
  commit: undefined,
  refactoringTypes: [],
  elementTypes: []
})
export const getters = getterTree(state, {})
export const mutations = mutationTree(state, {
  setDraft: (state, draft: Draft) => {
    state.draft = draft
  },
  setCommit: (state, commit: CommitInfo) => {
    state.commit = commit
  },
  setRefactoringTypes: (state, types: RefactoringType[]) => {
    state.refactoringTypes = types
  },
  setElementTypes: (state, types: string[]) => {
    state.elementTypes = types
  }
})
export const actions = actionTree(
  { state, getters, mutations },
  {
    async fetchDraft({ commit }, id: number) {
      const draft = await this.$client.getDraft(id)
      await commit('setDraft', draft)
      return draft
    },
    async fetchCommit({ commit }, sha: string) {
      await commit('setCommit', await this.$client.getCommitInfo(sha))
    },
    async fetchRefactoringTypes({ commit }) {
      await commit(
        'setRefactoringTypes',
        await this.$client.getRefactoringTypes()
      )
    },
    async fetchElementTypes({ commit }) {
      await commit('setElementTypes', await this.$client.getElementTypes())
    }
  }
)
