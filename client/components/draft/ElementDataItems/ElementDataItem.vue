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
            v-else-if="elementData.required"
            x-small
            color="error"
            title="This element is required"
            >fa-fw fa-asterisk</v-icon
          >
        </div>
        <v-list-item-content>
          <v-list-item-title>{{ elementKey }}</v-list-item-title>
          <v-list-item-subtitle>{{
            `${elementData.type}${elementData.multiple ? '[]' : ''}`
          }}</v-list-item-subtitle>
        </v-list-item-content>
        <div v-if="isDeletable">
          <v-btn icon x-small color="error" @click.stop="deleteElementKey">
            <v-icon x-small>fa-trash</v-icon>
          </v-btn>
        </div>
      </template>
      <div v-for="(element, i) in elementData.elements" :key="i">
        <v-divider />
        <element-location
          :category="category"
          :element-key="elementKey"
          :element-index="i"
          :element="element"
          :multiple="elementData.multiple"
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
import { defineComponent, computed } from '@nuxtjs/composition-api'
import { CodeElementHolder, DiffCategory } from 'refactorhub'
import { deleteElementDecoration } from '@/components/draft/ElementEditor/use/elementDecorations'
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
      type: String as () => DiffCategory,
      required: true,
    },
    elementKey: {
      type: String,
      required: true,
    },
    elementData: {
      type: Object as () => CodeElementHolder,
      required: true,
    },
    isDeletable: {
      type: Boolean,
      required: true,
    },
  },
  setup(props, { root }) {
    const isCompleted = computed(
      () =>
        props.elementData.elements.length > 0 &&
        props.elementData.elements.every((e) => !!e.location.path)
    )

    const draft = computed(() => root.$accessor.draft.draft)
    const deleteElementKey = async () => {
      if (!draft.value) return
      await root.$accessor.draft.setDraft(
        await root.$client.removeElementKey(
          draft.value.id,
          props.category,
          props.elementKey
        )
      )
      props.elementData.elements.forEach((_, i) => {
        deleteElementDecoration(props.category, props.elementKey, i)
      })
    }
    return { isCompleted, deleteElementKey }
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
