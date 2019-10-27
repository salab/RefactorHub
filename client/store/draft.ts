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
  },
  updateDraft: (state, description?: string) => {
    if (!state.draft) return
    if (description) state.draft.description = description
  }
})
export const actions = actionTree(
  { state, getters, mutations },
  {
    async fetchDraft({ commit }, id: number) {
      await commit('setDraft', await this.$client.getDraft(id))
    },
    async fetchCommit(ctx, sha: string) {
      await ctx.commit('setCommit', await this.$client.getCommitInfo(sha))
    },
    async fetchRefactoringTypes(ctx) {
      await ctx.commit(
        'setRefactoringTypes',
        await this.$client.getRefactoringTypes()
      )
    },
    async fetchElementTypes(ctx) {
      await ctx.commit('setElementTypes', await this.$client.getElementTypes())
    }
  }
)
