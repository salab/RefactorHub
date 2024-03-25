<script setup lang="ts">
import { DiffCategory } from 'refactorhub'
import apis from '@/apis'

const props = defineProps({
  category: {
    type: String as () => DiffCategory,
    required: true,
  },
})

const elementTypes = computed(() => useAnnotation().codeElementTypes.value)

const elementKey = ref('')
const elementType = ref(undefined)
const multiple = ref(false)

const addElementKey = async () => {
  if (elementKey.value && elementType.value) {
    // TODO: error handling
    const { annotationId, snapshotId, changeId } =
      useAnnotation().currentIds.value
    if (!annotationId || !snapshotId || !changeId) return
    useAnnotation().updateChange(
      (
        await apis.parameters.putNewParameter(
          annotationId,
          snapshotId,
          changeId,
          props.category,
          {
            parameterName: elementKey.value,
            elementType: elementType.value,
            multiple: multiple.value,
          },
        )
      ).data,
    )
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
      <span class="text-none body-1">Add Parameter</span>
    </v-btn>
  </div>
</template>
