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
  <div class="d-flex justify-center">
    <v-tooltip location="top center" origin="auto" :open-delay="500">
      <template #activator="{ props: tooltipProps }">
        <v-btn
          v-bind="tooltipProps"
          color="success"
          variant="text"
          :size="16"
          icon
          @click="addLocation"
        >
          <v-icon :size="16" icon="$mdiPlusBox" />
        </v-btn>
      </template>
      Append element
    </v-tooltip>
  </div>
</template>
