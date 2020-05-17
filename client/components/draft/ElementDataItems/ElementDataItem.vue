<template>
  <div>
    <v-list-group
      class="element-data"
      :class="`element-data-${elementData.type}`"
      :value="true"
    >
      <template v-slot:activator>
        <div class="pl-2 pr-1">
          <v-icon v-if="isCompleted" x-small color="success" title="Completed"
            >fa-fw fa-check</v-icon
          >
          <v-icon
            v-else
            x-small
            color="error"
            title="This element is not completed"
            >fa-fw fa-exclamation</v-icon
          >
        </div>
        <v-list-item-content>
          <v-list-item-title>{{ elementKey }}</v-list-item-title>
          <v-list-item-subtitle>{{ elementData.type }}</v-list-item-subtitle>
        </v-list-item-content>
      </template>
      <div v-for="(element, i) in elementData.elements" :key="i">
        <v-divider />
        <element-location
          :category="category"
          :element-key="elementKey"
          :element-index="i"
          :element="element"
        />
      </div>
      <div v-if="elementData.multiple">
        <v-divider />
        <add-location-button :category="category" :element-key="elementKey" />
      </div>
    </v-list-group>
    <v-divider />
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType, computed } from '@vue/composition-api'
import { ElementData, DiffCategory } from 'refactorhub'
import ElementLocation from './ElementLocation/ElementLocation.vue'
import AddLocationButton from './AddLocationButton.vue'

export default defineComponent({
  name: 'ElementDataItem',
  components: {
    ElementLocation,
    AddLocationButton,
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
    elementData: {
      type: Object as PropType<ElementData>,
      required: true,
    },
  },
  setup(props) {
    const isCompleted = computed(
      () =>
        props.elementData.elements.length > 0 &&
        props.elementData.elements.every((e) => !!e.location.path)
    )
    return { isCompleted }
  },
})
</script>

<style lang="scss" scoped>
.element-data {
  border-left: solid 0.5rem;
  &::v-deep {
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
