<template>
  <div>
    <v-list-group
      append-icon=""
      class="element-data"
      :class="`element-data-${elementData.type}`"
      :value="true"
    >
      <template v-slot:activator>
        <v-list-item-content>
          <v-list-item-title>{{ elementKey }}</v-list-item-title>
          <v-list-item-subtitle>{{ elementData.type }}</v-list-item-subtitle>
        </v-list-item-content>
        <v-list-item-action class="ml-0 justify-center">
          <v-icon
            v-if="elementData.elements.length > 0"
            small
            color="success"
            title="Completed"
            >fa-check</v-icon
          >
          <v-icon v-else small color="error" title="This is not completed"
            >fa-exclamation</v-icon
          >
        </v-list-item-action>
      </template>
      <element-location
        v-for="(element, i) in elementData.elements"
        :key="i"
        :element="element"
      />
    </v-list-group>
    <v-divider />
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from '@vue/composition-api'
import { ElementData } from 'refactorhub'
import ElementLocation from './ElementLocation.vue'

export default defineComponent({
  name: 'ElementDataItem',
  components: {
    ElementLocation,
  },
  props: {
    elementKey: {
      type: String,
      required: true,
    },
    elementData: {
      type: Object as PropType<ElementData>,
      required: true,
    },
  },
})
</script>

<style lang="scss" scoped>
.element-data {
  border-left: solid 12px;
}

.element-data ::v-deep .v-list-group__header {
  padding: 0 8px;
}
</style>
