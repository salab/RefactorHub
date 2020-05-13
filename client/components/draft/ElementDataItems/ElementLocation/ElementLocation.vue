<template>
  <div>
    <v-divider />
    <div class="d-flex pl-3">
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
      <div class="px-1">
        <!-- TODO: Impl
        <v-btn
          block
          x-small
          depressed
          tile
          class="px-1 my-1"
          @click="previewLocation"
        >
          <span class="text-none font-weight-regular">preview</span>
        </v-btn>
        -->
        <v-btn
          block
          x-small
          depressed
          tile
          class="px-1 my-1"
          @click="startEditLocation"
        >
          <span class="text-none font-weight-regular">edit</span>
        </v-btn>
        <v-btn
          block
          x-small
          depressed
          tile
          class="px-1 my-1"
          @click="deleteLocation"
        >
          <span class="text-none font-weight-regular">delete</span>
        </v-btn>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, computed, PropType } from '@vue/composition-api'
import { Element, DiffCategory } from 'refactorhub'
import NumberColumn from './NumberColumn.vue'
import { trimFileName } from '@/components/common/editor/use/trim'

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
  },
  setup(props, { root }) {
    const path = computed(
      () => trimFileName(props.element.location.path, 20) || '-'
    )

    const previewLocation = async () => {}
    const deleteLocation = async () => {}

    const startEditLocation = async () => {
      await root.$accessor.draft.setEditingElementMetadata({
        category: props.category,
        metadata: {
          key: props.elementKey,
          index: props.elementIndex,
          type: props.element.type,
        },
      })
    }

    return { path, previewLocation, deleteLocation, startEditLocation }
  },
})
</script>
