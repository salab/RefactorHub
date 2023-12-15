<script setup lang="ts">
import { DiffCategory } from 'refactorhub'
import apis, { CodeElementHolder, CodeElementMetadata } from '@/apis'

const props = defineProps({
  category: {
    type: String as () => DiffCategory,
    required: true,
  },
  elementKey: {
    type: String,
    required: true,
  },
  elementHolder: {
    type: Object as () => CodeElementHolder,
    required: true,
  },
  elementMetadata: {
    type: Object as () => CodeElementMetadata,
    default: undefined,
  },
  isRemovable: {
    type: Boolean,
    required: true,
  },
})
const { canEditCurrentChange } = useAnnotation()

const currentChangeId = computed(() => useAnnotation().currentChange.value?.id)

const removeElementKey = async () => {
  const { annotationId, snapshotId, changeId } =
    useAnnotation().currentIds.value
  if (!annotationId || !snapshotId || !changeId) return
  useAnnotation().updateChange(
    (
      await apis.parameters.removeParameter(
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
  <div>
    <v-list-group
      class="element-holder"
      :class="`element-holder-${elementHolder.type}`"
      :value="elementKey"
    >
      <template #activator="{ props: vListGroupProps }">
        <v-list-item v-bind="vListGroupProps" class="pa-0">
          <v-container class="d-flex align-center pa-0 ma-0">
            <div class="mx-1 d-flex flex-column">
              <v-tooltip location="top center" origin="auto">
                <template #activator="{ props: tooltipProps }">
                  <v-icon
                    v-bind="tooltipProps"
                    :size="16"
                    icon="$mdiInformation"
                    color="info"
                    class="my-1"
                  />
                </template>
                <div class="text-h6">
                  {{ elementKey }}: {{ elementHolder.type
                  }}{{ elementHolder.multiple ? '[]' : '' }}
                </div>
                <div v-if="elementMetadata?.required" class="text-subtitle-2">
                  This parameter is mandatory required
                </div>
                <div v-else-if="isRemovable" class="text-subtitle-2">
                  This parameter is not defined by the change type
                </div>
                <div v-else class="text-subtitle-2">
                  This parameter is optional
                </div>
                <v-divider :thickness="5" />
                <div v-if="elementMetadata?.description" class="text-body-2">
                  <cite>{{ elementMetadata?.description }}</cite>
                </div>
              </v-tooltip>
              <v-btn
                v-if="canEditCurrentChange && isRemovable && currentChangeId"
                variant="text"
                icon
                :size="16"
                color="error"
                class="mb-1"
                @click="
                  (e: MouseEvent) => {
                    e.stopPropagation()
                    if (elementHolder.elements.every((e) => !e.location))
                      removeElementKey()
                  }
                "
              >
                <v-icon :size="16" icon="$mdiDelete" />
                <parameter-dialog
                  v-if="elementHolder.elements.some((e) => e.location)"
                  title="Are you sure you want to remove this parameter?"
                  :change-parameters-list="[
                    {
                      changeId: currentChangeId,
                      parameters: elementHolder.elements.map((_, index) => ({
                        category,
                        parameterName: elementKey,
                        elementIndex: index,
                      })),
                    },
                  ]"
                  :continue-button="{
                    text: 'remove',
                    color: 'error',
                    onClick: () => removeElementKey(),
                  }"
                />
              </v-btn>
            </div>
            <v-container class="pa-0 ma-0 d-flex flex-column align-left">
              <v-tooltip location="top center" origin="auto" :open-delay="500">
                <template #activator="{ props: tooltipProps }">
                  <v-list-item-title>
                    <span
                      v-bind="tooltipProps"
                      :class="
                        elementMetadata?.required
                          ? 'font-weight-black'
                          : isRemovable
                          ? 'font-italic'
                          : 'font-weight-regular'
                      "
                      style="font-size: small"
                      >{{ elementKey }}</span
                    ></v-list-item-title
                  >
                </template>
                {{ elementKey }}
              </v-tooltip>
              <v-list-item-subtitle
                ><span style="font-size: x-small">{{
                  `${elementHolder.type}${elementHolder.multiple ? '[]' : ''}`
                }}</span></v-list-item-subtitle
              >
            </v-container>
          </v-container>
        </v-list-item>
      </template>
      <div v-for="(element, i) in elementHolder.elements" :key="i">
        <v-divider />
        <element-value
          :category="category"
          :element-key="elementKey"
          :element-index="i"
          :element="element"
          :multiple="elementHolder.multiple"
        />
      </div>
      <div v-if="canEditCurrentChange && elementHolder.multiple">
        <v-divider />
        <element-value-append-button
          :category="category"
          :element-key="elementKey"
        />
      </div>
    </v-list-group>
    <v-divider />
  </div>
</template>

<style lang="scss">
.v-list-item__append > .v-icon {
  margin-inline-start: 0px;
}
</style>
