import { defineNuxtPlugin } from '#app'
import apis from '@/apis'
import { useUser } from '#imports'

export default defineNuxtPlugin(async () => {
  try {
    const me = (await apis.users.getMe()).data
    useUser().user.value = me
  } catch {
    // TODO
  }
})
