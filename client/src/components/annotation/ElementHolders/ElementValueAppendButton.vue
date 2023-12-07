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

const addLocation = async () => {
  const { annotationId, snapshotId, changeId } =
    useAnnotation().currentIds.value
  if (!annotationId || !snapshotId || !changeId) return
  useAnnotation().updateChange(
    (
      await apis.parameters.appendParameterElement(
        annotationId,
        snapshotId,
        changeId,
        props.category,
        props.elementKey,
      )
    ).data,
  )
}
</script>

<template>
  <v-btn variant="flat" block size="small" @click="addLocation">
    <span class="text-none body-2">Add Location</span>
  </v-btn>
</template>
