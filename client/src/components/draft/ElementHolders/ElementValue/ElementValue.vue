<template>
  <div :class="{ [`element-value-${element.type}`]: isEditing }" class="d-flex">
    <div class="location flex-grow-1 d-flex flex-column justify-center pa-2">
      <div
        v-if="'name' in element && typeof element['name'] === 'string'"
        class="d-flex justify-center"
      >
        <span class="caption text--primary">{{ element['name'] }}</span>
      </div>
      <div class="path d-flex justify-center">
        <span ref="pathRef" class="caption text--secondary" :title="path">{{
          path
        }}</span>
      </div>
      <element-range :range="range" />
    </div>
    <div class="pa-1 d-flex flex-column justify-center">
      <v-btn
        x-small
        icon
        title="Preview on editor"
        color="secondary"
        :disabled="!isExist"
        @click="openLocation"
      >
        <v-icon x-small>fa-fw fa-eye</v-icon>
      </v-btn>
      <v-btn
        x-small
        icon
        title="Start to select on editor"
        color="primary"
        @click="toggleEditing"
      >
        <v-icon x-small>fa-fw fa-pen</v-icon>
      </v-btn>
      <v-btn x-small icon title="Delete" color="error" @click="deleteElement">
        <v-icon x-small>fa-fw fa-trash</v-icon>
      </v-btn>
    </div>
  </div>
</template>

<script lang="ts">
import {
  defineComponent,
  computed,
  useContext,
  onMounted,
  ref,
} from '@nuxtjs/composition-api'
import { DiffCategory } from 'refactorhub'
import { trimFileName } from '@/components/common/editor/utils/trim'
import { deleteElementDecoration } from '@/components/draft/ElementEditor/ts/elementDecorations'
import apis, { CodeElement, ActionName, ActionType } from '@/apis'
import { log } from '@/utils/action'

export default defineComponent({
  props: {
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
  },
  setup(props) {
    const {
      app: { $accessor },
    } = useContext()

    const pathRef = ref<HTMLSpanElement>()
    onMounted(() => {
      pathRef.value?.scrollTo(1000, 0)
    })

    const path = computed(() => props.element.location?.path || '-')

    const fileIndex = computed(() =>
      $accessor.draft.commit?.files
        ?.map((f) => (props.category === 'before' ? f.previousName : f.name))
        ?.indexOf(props.element.location?.path || '')
    )

    const isExist = computed(
      () => fileIndex.value !== undefined && fileIndex.value >= 0
    )

    const openLocation = async () => {
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
        await $accessor.draft.setDisplayedFile({
          category: 'before',
          file: props.category === 'before' ? file : { index },
        })
        await $accessor.draft.setDisplayedFile({
          category: 'after',
          file: props.category === 'after' ? file : { index },
        })
      }
    }

    const deleteElement = async () => {
      if (!confirm('Are you sure you want to delete this element?')) return
      await $accessor.draft.setDraft(
        (
          await apis.drafts.deleteRefactoringDraftElementValue(
            props.draftId,
            props.category,
            props.elementKey,
            props.elementIndex
          )
        ).data
      )
      deleteElementDecoration(
        props.category,
        props.elementKey,
        props.elementIndex
      )
    }

    const isEditing = computed(() => {
      const metadata = $accessor.draft.editingElement[props.category]
      return (
        metadata?.key === props.elementKey &&
        metadata?.index === props.elementIndex
      )
    })

    const toggleEditing = async () => {
      if (!isEditing.value) {
        log(ActionName.ToggleEditingElement, ActionType.Client, {
          category: props.category,
          element: {
            key: props.elementKey,
            index: props.elementIndex,
            type: props.element.type,
          },
        })
        await $accessor.draft.setEditingElement({
          category: props.category,
          element: {
            key: props.elementKey,
            index: props.elementIndex,
            type: props.element.type,
          },
        })
      } else {
        log(ActionName.ToggleEditingElement, ActionType.Client, {
          category: props.category,
        })
        await $accessor.draft.setEditingElement({
          category: props.category,
        })
      }
    }

    const range = computed(() => props.element.location?.range)

    return {
      pathRef,
      path,
      trimFileName,
      isExist,
      openLocation,
      toggleEditing,
      deleteElement,
      isEditing,
      range,
    }
  },
})
</script>

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
