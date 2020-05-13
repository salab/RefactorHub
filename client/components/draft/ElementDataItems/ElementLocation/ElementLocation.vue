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
        <v-btn block x-small depressed tile class="px-1 my-1">
          <span class="text-none">preview</span>
        </v-btn>
        <v-btn block x-small depressed tile class="px-1 my-1">
          <span class="text-none">edit</span>
        </v-btn>
        <v-btn block x-small depressed tile class="px-1 my-1">
          <span class="text-none">delete</span>
        </v-btn>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, computed, PropType } from '@vue/composition-api'
import { Element } from 'refactorhub'
import NumberColumn from './NumberColumn.vue'
import { trimFileName } from '@/components/common/editor/use/trim'

export default defineComponent({
  name: 'ElementLocation',
  components: {
    NumberColumn,
  },
  props: {
    element: {
      type: Object as PropType<Element>,
      required: true,
    },
  },
  setup(props) {
    const path = computed(
      () => trimFileName(props.element.location.path, 20) || '-'
    )
    return { path }
  },
})
</script>
