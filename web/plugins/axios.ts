import { Context } from '@nuxt/types'

export default ({ $axios }: Context): void => {
  $axios.defaults.withCredentials = true
  $axios.defaults.xsrfCookieName = 'XSRF-TOKEN'
  $axios.defaults.xsrfHeaderName = 'X-XSRF-TOKEN'

  $axios.onRequest((config): void => {
    console.log(`${config.method} ${config.url}`)
  })

  $axios.onError((error): void => {
    console.log(`${error.name}: ${error.message}`)
  })
}
