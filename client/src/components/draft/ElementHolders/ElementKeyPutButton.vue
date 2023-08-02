<script setup lang="ts">
import { DiffCategory } from 'refactorhub'
import apis from '@/apis'

const props = defineProps({
  category: {
    type: String as () => DiffCategory,
    required: true,
  },
})

const draft = computed(() => useDraft().draft.value)
const elementTypes = computed(() => useDraft().elementTypes.value)

const elementKey = ref('')
const elementType = ref(undefined)
const multiple = ref(false)

const addElementKey = async () => {
  if (draft.value && elementKey.value && elementType.value) {
    // TODO: error handling
    useDraft().draft.value = (
      await apis.drafts.putRefactoringDraftElementKey(
        draft.value.id,
        props.category,
        {
          key: elementKey.value,
          type: elementType.value,
          multiple: multiple.value,
        },
      )
    ).data
    elementKey.value = ''
    elementType.value = undefined
  }
}
</script>

<template>
  <div>
    <div class="px-2">
      <v-text-field
        v-model="elementKey"
        variant="underlined"
        clearable
        label="element name"
        density="compact"
        single-line
        hide-details
      />
      <v-select
        v-model="elementType"
        variant="underlined"
        clearable
        :items="elementTypes"
        label="ElementType"
        density="compact"
        single-line
        hide-details
      />
      <v-checkbox
        v-model="multiple"
        label="multiple"
        density="compact"
        hide-details
      />
    </div>
    <v-divider />
    <v-btn
      :rounded="0"
      block
      size="small"
      color="secondary"
      @click="addElementKey"
    >
      <span class="text-none body-1">Add Element</span>
    </v-btn>
  </div>
</template>
