<template>
  <div :class="{ [`element-value-${element.type}`]: isEditing }" class="d-flex">
    <div class="location flex-grow-1 d-flex flex-column justify-center pa-2">
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
        @click="toggleEditLocation"
      >
        <v-icon x-small>fa-fw fa-pen</v-icon>
      </v-btn>
      <v-btn
        v-if="multiple"
        x-small
        icon
        title="Delete"
        color="error"
        @click="deleteLocation"
      >
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
import apis, { CodeElement } from '@/apis'

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
        await $accessor.draft.setDisplayedFile({
          category: props.category,
          file: { index },
        })
      }
    }

    const deleteLocation = async () => {
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

    const toggleEditLocation = async () => {
      if (!isEditing.value) {
        await $accessor.draft.setEditingElement({
          category: props.category,
          element: {
            key: props.elementKey,
            index: props.elementIndex,
            type: props.element.type,
          },
        })
      } else {
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
      deleteLocation,
      toggleEditLocation,
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
