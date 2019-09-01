import { Context } from '@nuxt/types'

export default ({ $axios }: Context): void => {
  $axios.defaults.withCredentials = true
  $axios.defaults.xsrfCookieName = 'XSRF-TOKEN'
  $axios.defaults.xsrfHeaderName = 'X-XSRF-TOKEN'
}
