import Vue from 'vue'
import { Editor } from '~/plugins/editor'

declare module 'vue/types/vue' {
  interface Vue {
    $editor: Editor
  }
}

declare module '@nuxt/types' {
  interface NuxtAppOptions {
    $editor: Editor
  }
}

declare module 'vuex/types/index' {
  interface Store<S> {
    $editor: Editor
  }
}
