<template>
  <v-btn outlined tile small class="text-none" @click="edit">
    {{ refactoring.type.name }} (id={{ refactoring.id }})
  </v-btn>
</template>

<script lang="ts">
import { defineComponent } from '@nuxtjs/composition-api'
import { Refactoring } from 'refactorhub'

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
      const draft = await root.$client.editRefactoring(props.refactoring.id)
      root.$router.push(`/draft/${draft.id}`)
    }
    return { edit }
  },
})
</script>
