<script setup lang="ts">
import { DiffCategory } from 'refactorhub'
import apis from '@/apis'

const props = defineProps({
  category: {
    type: String as () => DiffCategory,
    required: true,
  },
  elementKey: {
    type: String,
    required: true,
  },
})

const draft = computed(() => useDraft().draft.value)
const addLocation = async () => {
  if (draft.value) {
    useDraft().draft.value = (
      await apis.drafts.appendCodeElementDefaultValue(
        draft.value.id,
        props.category,
        props.elementKey,
      )
    ).data
  }
}
</script>

<template>
  <v-btn variant="flat" block size="small" @click="addLocation">
    <span class="text-none body-2">Add Location</span>
  </v-btn>
</template>
