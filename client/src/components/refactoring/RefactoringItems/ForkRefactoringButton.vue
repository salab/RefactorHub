<template>
  <v-btn outlined tile small class="text-none" @click="fork">
    <v-icon small left>fa-code-branch</v-icon>
    Fork
  </v-btn>
</template>

<script lang="ts">
import { defineComponent } from '@nuxtjs/composition-api'
import apis, { Refactoring } from '@/apis'

export default defineComponent({
  name: 'ForkRefactoringButton',
  props: {
    refactoring: {
      type: Object as () => Refactoring,
      required: true,
    },
  },
  setup(props, { root }) {
    const fork = async () => {
      const draft = (
        await apis.refactorings.forkRefactoring(props.refactoring.id)
      ).data
      root.$router.push(`/draft/${draft.id}`)
    }
    return { fork }
  },
})
</script>
