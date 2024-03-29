<script setup lang="ts">
import { DiffCategory } from 'refactorhub'
import apis, { CodeElement, ActionName } from '@/apis'
import { sendAction } from '@/utils/action'
import { asMonacoRange } from '@/components/common/editor/utils/range'

const props = defineProps({
  category: {
    type: String as () => DiffCategory,
    required: true,
  },
  elementKey: {
    type: String,
    required: true,
  },
  elementIndex: {
    type: Number,
    required: true,
  },
  element: {
    type: Object as () => CodeElement,
    required: true,
  },
  multiple: {
    type: Boolean,
    required: true,
  },
})

const range = computed(() => props.element.location?.range)

const pathRef = ref<HTMLSpanElement>()
onMounted(() => {
  pathRef.value?.scrollTo(1000, 0)
  watch(
    () => props.element.location?.path,
    () => setTimeout(() => pathRef.value?.scrollTo(1000, 0), 100),
  )
})

const valueChangedTooltip = ref(false)
const valueChanged = ref(false)
const oldLocation = ref(props.element.location)
const oldCurrentChangeId = ref(useAnnotation().currentChange.value?.id)
function isNewLocation(newLocation: CodeElement['location']) {
  if (!oldLocation.value && !newLocation) return false
  if (!!oldLocation.value && !newLocation) {
    oldLocation.value = newLocation
    return false
  }
  if (!oldLocation.value && !!newLocation) {
    oldLocation.value = newLocation
    return true
  }
  if (!oldLocation.value || !newLocation) return false
  if (oldLocation.value.path !== newLocation.path) {
    oldLocation.value = newLocation
    return true
  }
  if (!oldLocation.value.range && !newLocation.range) return false
  if (!!oldLocation.value.range && !newLocation.range) {
    oldLocation.value = newLocation
    return true
  }
  if (!oldLocation.value.range && !!newLocation.range) {
    oldLocation.value = newLocation
    return true
  }
  if (!oldLocation.value.range || !newLocation.range) return false
  if (
    asMonacoRange(oldLocation.value.range).equalsRange(
      asMonacoRange(newLocation.range),
    )
  ) {
    return false
  }
  oldLocation.value = newLocation
  return true
}
watch(
  () => props.element.location,
  (newLocation) => {
    if (!isNewLocation(newLocation)) return
    if (useAnnotation().currentChange.value?.id !== oldCurrentChangeId.value) {
      oldCurrentChangeId.value = useAnnotation().currentChange.value?.id
      return
    }
    valueChanged.value = true
    valueChangedTooltip.value = true
    useParameter().addAutoHighlightedElement(props.category, {
      key: props.elementKey,
      index: props.elementIndex,
      type: props.element.type,
    })
    setTimeout(() => {
      valueChanged.value = false
      valueChangedTooltip.value = false
    }, 2000)
  },
)
watch(
  () => valueChangedTooltip.value,
  (newValue) => {
    if (!newValue) return
    if (!valueChanged.value) valueChangedTooltip.value = false
  },
)

const path = computed(() => props.element.location?.path || '-')

const file = computed(
  () => useAnnotation().getCurrentFilePair(path.value)?.[props.category],
)
const currentChangeId = computed(() => useAnnotation().currentChange.value?.id)

const isExisting = computed(() => file.value !== undefined)
const { canEditCurrentChange } = useAnnotation()

const openLocation = () => {
  const { viewers, updateViewer, mainViewerId } = useViewer()
  const mainViewer = viewers.value.find(
    (viewer) => viewer.id === mainViewerId.value,
  )
  const range = props.element.location?.range
  if (file.value && mainViewer) {
    const filePair = useAnnotation().getCurrentFilePair(file.value.path)
    if (!filePair) return
    sendAction(ActionName.OpenElementLocation, {
      category: props.category,
      parameterName: props.elementKey,
      elementIndex: props.elementIndex,
      range,
    })
    updateViewer(
      mainViewerId.value,
      mainViewer.type === 'file' &&
        (!useAnnotation().isDividingChange.value || props.category === 'before')
        ? {
            type: 'file',
            filePair,
            navigation: {
              category: props.category,
              range: asMonacoRange(range),
            },
          }
        : {
            type: 'diff',
            filePair,
            navigation: {
              category: props.category,
              range: asMonacoRange(range),
            },
          },
    )
  }
}

const deleteElement = async () => {
  const { annotationId, snapshotId, changeId } =
    useAnnotation().currentIds.value
  if (!annotationId || !snapshotId || !changeId) return
  useAnnotation().updateChange(
    (
      await apis.parameters.clearParameterValue(
        annotationId,
        snapshotId,
        changeId,
        props.category,
        props.elementKey,
        props.elementIndex,
      )
    ).data,
  )
}

const isHovered = computed(() => {
  const metadata = useParameter().hoveredElement.value[props.category]
  return (
    metadata?.key === props.elementKey && metadata?.index === props.elementIndex
  )
})
const isEditing = computed(() => {
  const metadata = useParameter().editingElement.value[props.category]
  return (
    metadata?.key === props.elementKey && metadata?.index === props.elementIndex
  )
})
const isAutoHighlighted = computed(() => {
  const elements = useParameter().autoHighlightedElements.value[props.category]
  return elements.some(
    (metadata) =>
      metadata.key === props.elementKey &&
      metadata.index === props.elementIndex,
  )
})

const toggleEditing = () => {
  if (!isEditing.value) {
    useParameter().updateEditingElement(props.category, {
      key: props.elementKey,
      index: props.elementIndex,
      type: props.element.type,
    })
    sendAction(ActionName.ToggleEditingElement, {
      category: props.category,
      parameterName: props.elementKey,
      elementIndex: props.elementIndex,
      type: props.element.type,
      toEdit: true,
    })
  } else {
    useParameter().updateEditingElement(props.category, undefined)
    sendAction(ActionName.ToggleEditingElement, {
      category: props.category,
      parameterName: props.elementKey,
      elementIndex: props.elementIndex,
      type: props.element.type,
      toEdit: false,
    })
  }
}
</script>

<template>
  <v-tooltip v-model="valueChangedTooltip" location="bottom">
    <template #activator="{ props: valueChangedTooltipProps }">
      <div
        v-bind="valueChangedTooltipProps"
        :class="{
          ['element-value-hovered']: isHovered || isAutoHighlighted,
          ['element-value-editing']: isEditing,
        }"
        class="d-flex"
        @mouseenter="
          () => {
            useParameter().updateHoveredElement(props.category, {
              key: props.elementKey,
              index: props.elementIndex,
              type: props.element.type,
            })
            sendAction(ActionName.HoverElement, {
              category: props.category,
              parameterName: props.elementKey,
              elementIndex: props.elementIndex,
              type: props.element.type,
              hovering: true,
            })
          }
        "
        @mouseleave="
          () => {
            if (isHovered) {
              useParameter().updateHoveredElement(props.category, undefined)
            }
            sendAction(ActionName.HoverElement, {
              category: props.category,
              parameterName: props.elementKey,
              elementIndex: props.elementIndex,
              type: props.element.type,
              hovering: false,
            })
          }
        "
      >
        <div
          class="location flex-grow-1 d-flex flex-column justify-center pa-2"
        >
          <div
            v-if="'name' in element && typeof element['name'] === 'string'"
            class="d-flex justify-center"
          >
            <v-tooltip location="top center" origin="auto" :open-delay="500">
              <template #activator="{ props: tooltipProps }">
                <span
                  v-bind="tooltipProps"
                  style="font-size: small"
                  class="d-inline-block text-truncate"
                  >{{ element['name'] }}</span
                >
              </template>
              {{ element['name'] }}
            </v-tooltip>
          </div>
          <div class="d-flex justify-center">
            <v-tooltip location="top center" origin="auto" :open-delay="500">
              <template #activator="{ props: tooltipProps }">
                <span
                  v-bind="tooltipProps"
                  ref="pathRef"
                  style="font-size: x-small; color: gray"
                  class="path"
                  >{{ path }}</span
                >
              </template>
              {{ path }}
            </v-tooltip>
          </div>
          <element-range :range="range" />
        </div>
        <div class="pa-1 d-flex flex-column justify-space-evenly">
          <v-tooltip location="top center" origin="auto" :open-delay="500">
            <template #activator="{ props: tooltipProps }">
              <v-btn
                v-bind="tooltipProps"
                variant="text"
                :size="16"
                icon
                :disabled="!isExisting"
                @click="openLocation"
              >
                <v-icon :size="16" icon="$mdiEyeOutline" />
              </v-btn>
            </template>
            Preview on editor
          </v-tooltip>
          <v-tooltip location="top center" origin="auto" :open-delay="500">
            <template #activator="{ props: tooltipProps }">
              <v-btn
                v-if="canEditCurrentChange"
                v-bind="tooltipProps"
                variant="text"
                :size="16"
                icon
                :color="!isEditing ? `secondary` : vuetifyColors.pink.accent2"
                @click="toggleEditing"
              >
                <v-icon v-if="!isEditing" :size="16" icon="$mdiMarker" />
                <v-icon v-else :size="16" icon="$mdiMarkerCancel" />
              </v-btn>
            </template>
            {{
              !isEditing
                ? 'Start to select on editor'
                : 'Cancel selecting on editor'
            }}
          </v-tooltip>
          <v-tooltip location="top center" origin="auto" :open-delay="500">
            <template #activator="{ props: tooltipProps }">
              <v-btn
                v-if="
                  canEditCurrentChange &&
                  currentChangeId &&
                  (multiple || element.location)
                "
                v-bind="tooltipProps"
                variant="text"
                :size="16"
                icon
                color="error"
                @click="
                  () => {
                    if (!element.location) deleteElement()
                  }
                "
              >
                <v-icon
                  :size="16"
                  :icon="multiple ? '$mdiDelete' : '$mdiEraser'"
                />
                <parameter-dialog
                  v-if="element.location"
                  :title="`Are you sure you want to ${
                    props.multiple ? 'delete' : 'clear'
                  } this parameter?`"
                  :change-parameters-list="[
                    {
                      changeId: currentChangeId,
                      parameters: [
                        {
                          category,
                          parameterName: elementKey,
                          elementIndex,
                        },
                      ],
                    },
                  ]"
                  :continue-button="{
                    text: multiple ? 'delete' : 'clear',
                    color: 'error',
                    onlyValidAnnotation: false,
                    onClick: () => deleteElement(),
                  }"
                />
              </v-btn>
            </template>
            {{ multiple ? 'Delete' : 'Clear' }}
          </v-tooltip>
        </div>
      </div>
    </template>
    <span class="font-h6">Location is Set</span>
  </v-tooltip>
</template>

<style lang="scss" scoped>
$scrollbar-width: 4px;

.location {
  overflow-x: hidden;
}
.path {
  display: flex;
  overflow-x: scroll;
  white-space: nowrap;
  scrollbar-width: none;
  &::-webkit-scrollbar {
    display: none;
  }
}
</style>
