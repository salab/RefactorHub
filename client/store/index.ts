import { DefineGetters, DefineMutations, DefineActions } from 'vuex-type-helper'
import { Draft, CommitInfo } from '~/types'

export interface DraftState {
  draft?: Draft
  commit?: CommitInfo
  refactoringTypes: string[]
}
export interface DraftGetters {}
export interface DraftMutations {
  setDraft: { draft: Draft }
  setCommit: { commit: CommitInfo }
  setRefactoringTypes: { types: string[] }
  updateDraft: { description?: string }
}
export interface DraftActions {
  fetchDraft: { id: string }
  fetchCommit: { sha: string }
  fetchRefactoringTypes: {}
}

export const state = (): DraftState => ({
  draft: undefined,
  commit: undefined,
  refactoringTypes: []
})
export const getters: DefineGetters<DraftGetters, DraftState> = {}
export const mutations: DefineMutations<DraftMutations, DraftState> = {
  setDraft: (state, { draft }) => {
    state.draft = draft
  },
  setCommit: (state, { commit }) => {
    state.commit = commit
  },
  setRefactoringTypes: (state, { types }) => {
    state.refactoringTypes = types
  },
  updateDraft: (state, { description }) => {
    if (!state.draft) return
    if (description) state.draft.description = description
  }
}
export const actions: DefineActions<
  DraftActions,
  DraftState,
  DraftMutations,
  DraftGetters
> = {
  async fetchDraft(ctx, { id }) {
    const { data } = await this.$axios.get<Draft>(`/api/draft/${id}`)
    await ctx.commit({ type: 'setDraft', draft: data })
    await ctx.dispatch({ type: 'fetchCommit', sha: data.commit.sha })
  },
  async fetchCommit(ctx, { sha }) {
    const { data } = await this.$axios.get<CommitInfo>(
      `/api/commit/${sha}/info`
    )
    await ctx.commit({ type: 'setCommit', commit: data })
  },
  async fetchRefactoringTypes(ctx) {
    const { data } = await this.$axios.get<string[]>('/api/refactoring/types')
    ctx.commit({ type: 'setRefactoringTypes', types: data })
  }
}
