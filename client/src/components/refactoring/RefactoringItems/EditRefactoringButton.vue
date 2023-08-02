<script setup lang="ts">
import apis, { Refactoring } from '@/apis'

const props = defineProps({
  refactoring: {
    type: Object as () => Refactoring,
    required: true,
  },
})

const edit = async () => {
  const draft = (await apis.refactorings.editRefactoring(props.refactoring.id))
    .data
  navigateTo(`/draft/${draft.id}`)
}
</script>

<template>
  <v-btn outlined tile small class="text-none" @click="edit">
    {{ refactoring.type }} (id={{ refactoring.id }})
  </v-btn>
</template>
