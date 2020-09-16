<template>
  <div>
    <refactoring-item
      v-for="refactoring in refactorings"
      :key="refactoring.id"
      :refactoring="refactoring"
      class="my-4"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent } from '@nuxtjs/composition-api'
import { Refactoring } from 'refactorhub'
import RefactoringItem from './RefactoringItem.vue'

export default defineComponent({
  name: 'RefactoringItems',
  components: {
    RefactoringItem,
  },
  props: {
    refactorings: {
      type: Array as () => Refactoring[],
      required: true,
    },
  },
})
</script>
