<template>
  <v-navigation-drawer :right="category === 'after'" :width="180" permanent>
    <v-list-item>
      <v-list-item-content>
        <v-list-item-title class="font-weight-medium">
          {{ title }} Elements
        </v-list-item-title>
      </v-list-item-content>
    </v-list-item>
    <v-divider />
    <v-list expand>
      <v-divider />
      <element-data-item
        v-for="(data, key) in elementDataMap"
        :key="key"
        :element-key="key"
        :element-data="data"
      />
    </v-list>
  </v-navigation-drawer>
</template>

<script lang="ts">
import { defineComponent, PropType, computed } from '@vue/composition-api'
import { capitalize } from 'lodash'
import { DiffCategory } from 'refactorhub'
import ElementDataItem from './ElementDataItem.vue'

export default defineComponent({
  name: 'ElementDataItems',
  components: {
    ElementDataItem,
  },
  props: {
    category: {
      type: String as PropType<DiffCategory>,
      required: true,
    },
  },
  setup(props, { root }) {
    const title = capitalize(props.category)

    const draft = computed(() => root.$accessor.draft.draft)
    const elementDataMap = computed(() => {
      if (draft.value) {
        return props.category === 'before'
          ? draft.value.data.before
          : draft.value.data.after
      }
      return {}
    })

    return {
      title,
      elementDataMap,
    }
  },
})
</script>
