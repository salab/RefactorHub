<script setup lang="ts">
import { DiffCategory } from 'refactorhub'
import apis, { CodeElement, ActionName, ActionType } from '@/apis'
import { log } from '@/utils/action'
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
    log(ActionName.OpenElementLocation, ActionType.Client, {
      category: props.category,
      file: file.value,
    })
    updateViewer(
      mainViewerId.value,
      mainViewer.type === 'file'
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

const toggleEditing = () => {
  if (!isEditing.value) {
    log(ActionName.ToggleEditingElement, ActionType.Client, {
      category: props.category,
      element: {
        key: props.elementKey,
        index: props.elementIndex,
        type: props.element.type,
      },
    })
    useParameter().updateEditingElement(props.category, {
      key: props.elementKey,
      index: props.elementIndex,
      type: props.element.type,
    })
  } else {
    log(ActionName.ToggleEditingElement, ActionType.Client, {
      category: props.category,
    })
    useParameter().updateEditingElement(props.category, undefined)
  }
}
</script>

<template>
  <div
    :class="{
      ['element-value-hovered']: isHovered,
      ['element-value-editing']: isEditing,
    }"
    class="d-flex"
    @mouseenter="
      () =>
        useParameter().updateHoveredElement(props.category, {
          key: props.elementKey,
          index: props.elementIndex,
          type: props.element.type,
        })
    "
    @mouseleave="
      () => {
        if (isHovered)
          useParameter().updateHoveredElement(props.category, undefined)
      }
    "
  >
    <div class="location flex-grow-1 d-flex flex-column justify-center pa-2">
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
            color="secondary"
            @click="toggleEditing"
          >
            <v-icon :size="16" icon="$mdiMarker" />
          </v-btn>
        </template>
        Start to select on editor
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
            <v-icon :size="16" :icon="multiple ? '$mdiDelete' : '$mdiEraser'" />
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

<style lang="scss" scoped>
$scrollbar-width: 4px;

.location {
  overflow-x: hidden;
}
.path {
  display: flex;
  overflow-x: scroll;
  white-space: nowrap;
}
</style>
