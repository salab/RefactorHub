<script setup lang="ts">
import apis, { Refactoring } from '@/apis'

const props = defineProps({
  refactoring: {
    type: Object as () => Refactoring,
    required: true,
  },
})

const fork = async () => {
  try {
    const draft = (
      await apis.refactorings.forkRefactoring(props.refactoring.id)
    ).data
    navigateTo(`/draft/${draft.id}`)
  } catch (e) {
    location.href = '/login'
  }
}
</script>

<template>
  <v-btn outlined tile small class="text-none" @click="fork">
    <v-icon small left>fa-code-branch</v-icon>
    Fork
  </v-btn>
</template>
