<template>
  <v-btn outlined tile small class="text-none" @click="edit">
    {{ refactoring.type }} (id={{ refactoring.id }})
  </v-btn>
</template>

<script lang="ts">
import { defineComponent } from '@nuxtjs/composition-api'
import apis, { Refactoring } from '@/apis'

export default defineComponent({
  name: 'EditRefactoringButton',
  props: {
    refactoring: {
      type: Object as () => Refactoring,
      required: true,
    },
  },
  setup(props, { root }) {
    const edit = async () => {
      const draft = (
        await apis.refactorings.editRefactoring(props.refactoring.id)
      ).data
      root.$router.push(`/draft/${draft.id}`)
    }
    return { edit }
  },
})
</script>
