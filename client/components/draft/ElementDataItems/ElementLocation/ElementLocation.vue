<template>
  <div
    :class="{ [`element-location-${element.type}`]: isEditing }"
    class="d-flex pl-3"
  >
    <div class="flex-grow-1 d-flex flex-column justify-center">
      <span class="caption text--secondary">{{ path }}</span>
      <div class="d-flex align-center text--secondary">
        <number-column :number="element.location.range.startLine" />
        <div>:</div>
        <number-column :number="element.location.range.startColumn" />
        <div class="px-1">~</div>
        <number-column :number="element.location.range.endLine" />
        <div>:</div>
        <number-column :number="element.location.range.endColumn" />
      </div>
    </div>
    <div class="pa-1 buttons d-flex flex-column justify-center">
      <v-btn
        x-small
        outlined
        tile
        color="secondary"
        :disabled="!isExist"
        @click="openLocation"
      >
        <span class="text-none font-weight-regular">open</span>
      </v-btn>
      <v-btn
        x-small
        outlined
        tile
        color="primary"
        class="mt-1"
        @click="toggleEditLocation"
      >
        <span class="text-none font-weight-regular">edit</span>
      </v-btn>
      <v-btn
        v-if="multiple"
        x-small
        outlined
        tile
        color="error"
        class="mt-1"
        @click="deleteLocation"
      >
        <span class="text-none font-weight-regular">delete</span>
      </v-btn>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, computed, PropType } from '@vue/composition-api'
import { Element, DiffCategory } from 'refactorhub'
import NumberColumn from './NumberColumn.vue'
import { trimFileName } from '@/components/common/editor/use/trim'
import { deleteElementDecoration } from '@/components/draft/ElementEditor/use/elementDecorations'

export default defineComponent({
  name: 'ElementLocation',
  components: {
    NumberColumn,
  },
  props: {
    category: {
      type: String as PropType<DiffCategory>,
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
      type: Object as PropType<Element>,
      required: true,
    },
    multiple: {
      type: Boolean,
      required: true,
    },
  },
  setup(props, { root }) {
    const path = computed(
      () => trimFileName(props.element.location.path, 20) || '-'
    )

    const draft = computed(() => root.$accessor.draft.draft)

    const fileIndex = computed(() =>
      root.$accessor.draft.commitInfo?.files
        ?.map((f) => (props.category === 'before' ? f.previousName : f.name))
        ?.indexOf(props.element.location.path)
    )

    const isExist = computed(
      () => fileIndex.value !== undefined && fileIndex.value >= 0
    )

    const openLocation = async () => {
      const index = fileIndex.value
      if (index !== undefined && index >= 0) {
        await root.$accessor.draft.setDisplayedFileMetadata({
          category: props.category,
          metadata: { index },
        })
      }
    }

    const deleteLocation = async () => {
      if (!draft.value) return
      await root.$accessor.draft.setDraft(
        await root.$client.removeElementValue(
          draft.value.id,
          props.category,
          props.elementKey,
          props.elementIndex
        )
      )
      deleteElementDecoration(
        props.category,
        props.elementKey,
        props.elementIndex
      )
    }

    const isEditing = computed(() => {
      const metadata =
        root.$accessor.draft.editingElementMetadata[props.category]
      return (
        metadata?.key === props.elementKey &&
        metadata?.index === props.elementIndex
      )
    })

    const toggleEditLocation = async () => {
      if (!isEditing.value) {
        await root.$accessor.draft.setEditingElementMetadata({
          category: props.category,
          metadata: {
            key: props.elementKey,
            index: props.elementIndex,
            type: props.element.type,
          },
        })
      } else {
        await root.$accessor.draft.setEditingElementMetadata({
          category: props.category,
        })
      }
    }

    return {
      path,
      isExist,
      openLocation,
      deleteLocation,
      toggleEditLocation,
      isEditing,
    }
  },
})
</script>

<style lang="scss" scoped>
.buttons {
  width: 3.5rem;
}
</style>
