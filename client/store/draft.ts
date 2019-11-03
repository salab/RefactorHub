import { getterTree, mutationTree, actionTree } from 'nuxt-typed-vuex'
import * as monaco from 'monaco-editor'
import {
  Draft,
  CommitInfo,
  RefactoringType,
  TextModel,
  Diff,
  Element
} from 'refactorhub'

export const state = (): {
  draft?: Draft
  commit?: CommitInfo
  refactoringTypes: RefactoringType[]
  elementTypes: string[]
  textModels: Map<string, TextModel>
  file: { [key in Diff]?: number }
  element: { [key in Diff]?: [string, Element] }
  decorations: { [key in Diff]: Map<string, { id: string; uri: monaco.Uri }> }
} => ({
  draft: undefined,
  commit: undefined,
  refactoringTypes: [],
  elementTypes: [],
  textModels: new Map(),
  file: {
    before: undefined,
    after: undefined
  },
  element: {
    before: undefined,
    after: undefined
  },
  decorations: {
    before: new Map(),
    after: new Map()
  }
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
  setTextModel: (state, { uri, model }: { uri: string; model: TextModel }) => {
    state.textModels.set(uri, model)
  },
  setFile: (state, { diff, value }: { diff: Diff; value?: number }) => {
    state.file[diff] = value
  },
  setElement: (
    state,
    { diff, element }: { diff: Diff; element?: [string, Element] }
  ) => {
    state.element[diff] = element
  },
  setDecoration: (
    state,
    {
      diff,
      key,
      id,
      uri
    }: { diff: Diff; key: string; id: string; uri: monaco.Uri }
  ) => {
    state.decorations[diff].set(key, { id, uri })
  },
  deleteDecoration: (state, { diff, key }: { diff: Diff; key: string }) => {
    state.decorations[diff].delete(key)
  }
})
export const actions = actionTree(
  { state, getters, mutations },
  {
    async fetchDraft({ commit }, id: number) {
      const draft = await this.$client.getDraft(id)
      await commit('setDraft', draft)
      await this.app.$accessor.draft.fetchCommit(draft.commit.sha)
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
    },
    async getTextModel(
      { commit, state },
      {
        owner,
        repository,
        sha,
        path,
        uri
      }: {
        owner: string
        repository: string
        sha: string
        path: string
        uri: string
      }
    ) {
      if (state.textModels.has(uri)) {
        return state.textModels.get(uri)!!
      }
      const model = await this.$client.getTextModel(
        owner,
        repository,
        sha,
        path
      )
      await commit('setTextModel', {
        uri,
        model
      })
      return model
    }
  }
)
