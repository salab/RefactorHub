import apis from '@/apis'
import { Middleware } from '@nuxt/types'

const authenticated: Middleware = async (ctx) => {
  await apis.users.getMe().catch(() => {
    location.href = '/login'
  })
}

export default authenticated
