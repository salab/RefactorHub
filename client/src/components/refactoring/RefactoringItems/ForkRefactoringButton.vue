<template>
  <v-btn outlined tile small class="text-none" @click="fork">
    <v-icon small left>fa-code-branch</v-icon>
    Fork
  </v-btn>
</template>

<script lang="ts">
import { defineComponent, useContext } from '@nuxtjs/composition-api'
import apis, { Refactoring } from '@/apis'

export default defineComponent({
  props: {
    refactoring: {
      type: Object as () => Refactoring,
      required: true,
    },
  },
  setup(props) {
    const {
      app: { router },
    } = useContext()

    const fork = async () => {
      const draft = (
        await apis.refactorings.forkRefactoring(props.refactoring.id)
      ).data
      router?.push(`/draft/${draft.id}`)
    }
    return { fork }
  },
})
</script>
