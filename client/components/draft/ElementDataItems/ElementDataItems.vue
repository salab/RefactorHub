<template>
  <v-card flat tile>
    <v-navigation-drawer :right="category === 'after'" :width="200" permanent>
      <div class="d-flex flex-column fill-height">
        <div class="d-flex justify-center py-1">
          <span class="font-weight-medium">{{ title }} Elements</span>
        </div>
        <v-divider />
        <div class="flex-grow-1 list-container">
          <v-list expand class="py-0">
            <element-data-item
              v-for="(data, key) in elementDataMap"
              :key="key"
              :category="category"
              :element-key="key"
              :element-data="data"
            />
          </v-list>
        </div>
      </div>
    </v-navigation-drawer>
  </v-card>
</template>

<script lang="ts">
import { defineComponent, PropType, computed } from '@vue/composition-api'
import { capitalize } from 'lodash-es'
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

<style lang="scss" scoped>
.list-container {
  min-height: 0;
  overflow: scroll;
}
</style>
