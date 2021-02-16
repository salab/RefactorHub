import apis from '@/apis'
import { Plugin } from '@nuxt/types'

const auth: Plugin = async (ctx) => {
  try {
    const me = (await apis.users.getMe()).data
    ctx.app.$accessor.setUser(me)
  } catch {
    // TODO
  }
}

export default auth
