<script setup lang="ts">
import { DiffCategory } from 'refactorhub'
import { deleteElementDecoration } from '@/components/draft/ElementEditor/ts/elementDecorations'
import apis, { CodeElement, ActionName, ActionType } from '@/apis'
import { log } from '@/utils/action'

const props = defineProps({
  draftId: {
    type: Number,
    required: true,
  },
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
const pathRef = ref<HTMLSpanElement>()
onMounted(() => {
  pathRef.value?.scrollTo(1000, 0)
})

const path = computed(() => props.element.location?.path || '-')

const fileIndex = computed(
  () =>
    useDraft()
      .commit.value?.files?.map((f) =>
        props.category === 'before' ? f.previousName : f.name,
      )
      ?.indexOf(props.element.location?.path || ''),
)

const isExist = computed(
  () => fileIndex.value !== undefined && fileIndex.value >= 0,
)

const openLocation = () => {
  const index = fileIndex.value
  if (index !== undefined && index >= 0) {
    const file = {
      index,
      lineNumber: props.element.location?.range?.startLine,
    }
    log(ActionName.OpenElementLocation, ActionType.Client, {
      category: props.category,
      file,
    })
    useDraft().displayedFile.value.before =
      props.category === 'before' ? file : { index }
    useDraft().displayedFile.value.after =
      props.category === 'after' ? file : { index }
  }
}

const deleteElement = async () => {
  if (!confirm('Are you sure you want to delete this element?')) return
  useDraft().draft.value = (
    await apis.drafts.deleteRefactoringDraftElementValue(
      props.draftId,
      props.category,
      props.elementKey,
      props.elementIndex,
    )
  ).data
  deleteElementDecoration(props.category, props.elementKey, props.elementIndex)
}

const isEditing = computed(() => {
  const metadata = useDraft().editingElement.value[props.category]
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
    useDraft().editingElement.value[props.category] = {
      key: props.elementKey,
      index: props.elementIndex,
      type: props.element.type,
    }
  } else {
    log(ActionName.ToggleEditingElement, ActionType.Client, {
      category: props.category,
    })
    useDraft().editingElement.value[props.category] = undefined
  }
}

const range = computed(() => props.element.location?.range)
</script>

<template>
  <div :class="{ [`element-value-${element.type}`]: isEditing }" class="d-flex">
    <div class="location flex-grow-1 d-flex flex-column justify-center pa-2">
      <div
        v-if="'name' in element && typeof element['name'] === 'string'"
        class="d-flex justify-center"
      >
        <span style="font-size: small">{{ element['name'] }}</span>
      </div>
      <div class="path d-flex justify-center">
        <span
          ref="pathRef"
          style="font-size: x-small; color: gray"
          :title="path"
          >{{ path }}</span
        >
      </div>
      <element-range :range="range" />
    </div>
    <div class="pa-1 d-flex flex-column justify-space-evenly">
      <v-btn
        variant="text"
        :size="16"
        icon
        title="Preview on editor"
        :disabled="!isExist"
        @click="openLocation"
      >
        <v-icon :size="16" icon="$mdiEyeOutline" />
      </v-btn>
      <v-btn
        variant="text"
        :size="16"
        icon
        title="Start to select on editor"
        color="secondary"
        @click="toggleEditing"
      >
        <v-icon :size="16" icon="$mdiMarker" />
      </v-btn>
      <v-btn
        variant="text"
        :size="16"
        icon
        title="Delete"
        color="error"
        @click="deleteElement"
      >
        <v-icon :size="16" icon="$mdiDelete" />
      </v-btn>
    </div>
  </div>
</template>

<style lang="scss" scoped>
$scrollbar-width: 4px;

.location {
  overflow-x: hidden;

  .path {
    overflow-x: hidden;

    span {
      display: flex;
      overflow-x: scroll;
      white-space: nowrap;
      // max-width: 100%;
      &::-webkit-scrollbar {
        height: $scrollbar-width;
      }
      &::-webkit-scrollbar-thumb {
        background-color: rgba(128, 128, 128, 0.2);
        border-radius: $scrollbar-width;
      }
      &::-webkit-scrollbar-button {
        display: none;
      }
      &::-webkit-scrollbar-corner {
        display: none;
      }
    }
  }
}
</style>
