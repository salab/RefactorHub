import { Client } from '@/plugins/client'
import { Editor } from '@/plugins/editor'

declare module 'vue/types/vue' {
  interface Vue {
    $client: Client
    $editor: Editor
  }
}

declare module '@nuxt/types' {
  interface NuxtAppOptions {
    $client: Client
    $editor: Editor
  }
}

declare module 'vuex/types/index' {
  interface Store<S> {
    $client: Client
    $editor: Editor
  }
}
