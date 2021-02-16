import {
  getAccessorType,
  getterTree,
  mutationTree,
  actionTree,
} from 'nuxt-typed-vuex'

import * as draft from '@/store/draft'
import { User } from '@/apis/generated/api'

export const state = (): {
  user?: User
} => ({
  user: undefined,
})
export const getters = getterTree(state, {
  isAuthenticated: (state) => state.user !== undefined,
})
export const mutations = mutationTree(state, {
  setUser: (state, user?: User) => {
    state.user = user
  },
})
export const actions = actionTree({ state, getters, mutations }, {})

export const accessorType = getAccessorType({
  state,
  getters,
  mutations,
  actions,
  modules: {
    draft,
  },
})
