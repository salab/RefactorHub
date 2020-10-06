<template>
  <v-container>
    <v-row>
      <v-col>
        <h1>All Refactorings</h1>
        <v-divider class="mb-5" />
        <refactoring-items :refactorings="refactorings" />
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import { defineComponent, ref, useAsync } from '@nuxtjs/composition-api'
import { Refactoring } from 'refactorhub'
import RefactoringItems from '@/components/refactoring/RefactoringItems/RefactoringItems.vue'

export default defineComponent({
  name: 'all',
  components: {
    RefactoringItems,
  },
  setup(_, { root }) {
    const refactorings = ref<Refactoring[]>([])

    useAsync(async () => {
      refactorings.value = await root.$client.getRefactorings()
    })

    return { refactorings }
  },
})
</script>
