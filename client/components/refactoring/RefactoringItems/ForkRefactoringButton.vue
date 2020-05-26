<template>
  <v-btn outlined tile small class="text-none" @click="fork">
    <v-icon small left>fa-code-branch</v-icon>
    Fork
  </v-btn>
</template>

<script lang="ts">
import { defineComponent, PropType } from '@vue/composition-api'
import { Refactoring } from 'refactorhub'

export default defineComponent({
  name: 'ForkRefactoringButton',
  props: {
    refactoring: {
      type: Object as PropType<Refactoring>,
      required: true,
    },
  },
  setup(props, { root }) {
    const fork = async () => {
      const draft = await root.$client.forkRefactoring(props.refactoring.id)
      root.$router.push(`/draft/${draft.id}`)
    }
    return { fork }
  },
})
</script>
