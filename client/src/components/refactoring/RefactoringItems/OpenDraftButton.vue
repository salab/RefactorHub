<script setup lang="ts">
import { RefactoringDraft } from '@/apis'

const props = defineProps({
  draft: {
    type: Object as () => RefactoringDraft,
    required: true,
  },
})
const open = () => {
  navigateTo(`/draft/${props.draft.id}`)
}
</script>

<template>
  <v-btn outlined tile small class="text-none" @click="open">
    {{ draft.type }} (id={{ draft.id
    }}{{ draft.ownerId ? `, owner=${draft.ownerId}` : ''
    }}{{ draft.originId ? `, origin=${draft.originId}` : '' }})
  </v-btn>
</template>
