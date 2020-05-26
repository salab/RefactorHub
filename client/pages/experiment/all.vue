<template>
  <v-container>
    <v-row>
      <v-col>
        <h1>Experiments</h1>
        <v-divider class="mb-5" />
        <refactoring-items :refactorings="refactorings" />
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted } from '@vue/composition-api'
import { Refactoring } from 'refactorhub'
import RefactoringItems from '@/components/refactoring/RefactoringItems/RefactoringItems.vue'

export default defineComponent({
  name: 'experimentAll',
  components: {
    RefactoringItems,
  },
  setup(_, { root }) {
    const refactorings = ref<Refactoring[]>([])

    onMounted(async () => {
      refactorings.value = await root.$client.getUserRefactorings(2)
    })

    return { refactorings }
  },
})
</script>
