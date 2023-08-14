import { createVuetify } from 'vuetify'
import { aliases, mdi } from 'vuetify/iconsets/mdi-svg'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'
import {
  mdiArrowRightBoldBox,
  mdiMinusBox,
  mdiPencilBox,
  mdiPlusBox,
  mdiEyeOutline,
  mdiCloseCircleOutline,
  mdiAsterisk,
  mdiCheckCircle,
  mdiCircleOutline,
  mdiDelete,
  mdiMarker,
  mdiGithub,
} from '@mdi/js'

export default defineNuxtPlugin((nuxtApp) => {
  const vuetify = createVuetify({
    ssr: true,
    components,
    directives,
    icons: {
      defaultSet: 'mdi',
      aliases: {
        ...aliases,
        mdiArrowRightBoldBox,
        mdiMinusBox,
        mdiPencilBox,
        mdiPlusBox,
        mdiEyeOutline,
        mdiCloseCircleOutline,
        mdiAsterisk,
        mdiCheckCircle,
        mdiCircleOutline,
        mdiDelete,
        mdiMarker,
        mdiGithub,
      },
      sets: { mdi },
    },
  })

  nuxtApp.vueApp.use(vuetify)
})
