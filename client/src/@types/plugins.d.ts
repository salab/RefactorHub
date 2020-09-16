import { Client } from '@/plugins/client'

declare module 'vue/types/vue' {
  interface Vue {
    $client: Client
  }
}

declare module '@nuxt/types' {
  interface NuxtAppOptions {
    $client: Client
  }
}

declare module 'vuex/types/index' {
  interface Store<S> {
    $client: Client
  }
}
