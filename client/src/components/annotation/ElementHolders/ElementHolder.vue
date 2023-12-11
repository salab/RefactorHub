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
    default: null,
  },
  isRemovable: {
    type: Boolean,
    required: true,
  },
})
const isCompleted = computed(() => props.elementHolder.state === 'Manual')
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

const verifyElement = async () => {
  if (!canEditCurrentChange.value) return
  const { annotationId, snapshotId, changeId } =
    useAnnotation().currentIds.value
  if (!annotationId || !snapshotId || !changeId) return
  useAnnotation().updateChange(
    (
      await apis.parameters.verifyParameter(
        annotationId,
        snapshotId,
        changeId,
        props.category,
        props.elementKey,
        { isVerified: !isCompleted.value },
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
            <div class="mx-1 d-flex flex-column align-center">
              <v-icon
                v-if="elementMetadata && elementMetadata.required"
                class="my-1"
                :size="12"
                color="error"
                title="This element is required"
                icon="$mdiAsterisk"
              />
              <v-btn
                variant="text"
                :size="16"
                icon
                :title="
                  isCompleted
                    ? 'This element is verified'
                    : 'Verify this element'
                "
                @click.stop="verifyElement"
              >
                <v-icon
                  v-if="isCompleted"
                  :size="16"
                  color="success"
                  icon="$mdiCheckCircle"
                />
                <v-icon v-else :size="16" icon="$mdiCircleOutline" />
              </v-btn>
              <v-btn
                v-if="canEditCurrentChange && isRemovable && currentChangeId"
                variant="text"
                icon
                :size="16"
                color="error"
              >
                <v-icon :size="16" icon="$mdiDelete" />
                <parameter-dialog
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
              <v-list-item-title
                ><v-tooltip
                  v-if="elementMetadata?.description"
                  location="top center"
                  origin="auto"
                >
                  <template #activator="{ props: tooltipProps }">
                    <v-icon
                      v-bind="tooltipProps"
                      :size="14"
                      icon="$mdiInformation"
                      color="info"
                      class="me-1"
                    />
                  </template>
                  <div>{{ elementMetadata?.description }}</div>
                </v-tooltip>
                <span style="font-size: small">{{
                  elementKey
                }}</span></v-list-item-title
              >
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

<style lang="scss" scoped>
.element-holder {
  border-left: solid 0.5rem;
  ::v-deep(&) {
    .v-list-group__header {
      padding: 0;
    }
    .v-list-item__icon.v-list-group__header__append-icon {
      min-width: 0;
      margin: 0 0.5rem;
      .v-icon {
        font-size: 1rem;
      }
    }
  }
}
</style>
