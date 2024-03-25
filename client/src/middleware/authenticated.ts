import apis from '@/apis'

export default defineNuxtRouteMiddleware(async (to) => {
  await apis.users.getMe().catch(() => {
    if (to.path !== '/login') location.href = '/login'
  })
})
