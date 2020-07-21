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
              :is-deletable="isDeletable(key)"
            />
          </v-list>
        </div>
        <div>
          <v-divider />
          <add-element-key-button :category="category" />
        </div>
      </div>
    </v-navigation-drawer>
  </v-card>
</template>

<script lang="ts">
import {
  defineComponent,
  PropType,
  computed,
  ref,
  watch,
} from '@vue/composition-api'
import { capitalize } from 'lodash-es'
import { DiffCategory, RefactoringType } from 'refactorhub'
import ElementDataItem from './ElementDataItem.vue'
import AddElementKeyButton from './AddElementKeyButton.vue'

export default defineComponent({
  name: 'ElementDataItems',
  components: {
    ElementDataItem,
    AddElementKeyButton,
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

    const type = ref<RefactoringType>()
    watch(draft, async () => {
      if (draft.value) {
        type.value = await root.$client.getRefactoringType(draft.value.type)
      }
    })
    const elementMap = computed(() => {
      if (type.value) {
        return props.category === 'before'
          ? type.value.before
          : type.value.after
      }
      return {}
    })

    const isDeletable = (key: string) =>
      !Object.keys(elementMap.value).includes(key)

    return {
      title,
      elementDataMap,
      isDeletable,
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
