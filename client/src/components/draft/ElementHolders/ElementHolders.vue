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
            <element-holder
              v-for="(holder, key) in elementHolderMap"
              :key="key"
              :draft-id="draft.id"
              :category="category"
              :element-key="key"
              :element-holder="holder"
              :element-metadata="elementMetadataMap[key]"
              :is-removable="isRemovable(key)"
            />
          </v-list>
        </div>
        <div>
          <v-divider />
          <element-key-put-button :category="category" />
        </div>
      </div>
    </v-navigation-drawer>
  </v-card>
</template>

<script lang="ts">
import { defineComponent, computed, useContext } from '@nuxtjs/composition-api'
import { capitalize } from 'lodash-es'
import { DiffCategory } from 'refactorhub'
import { RefactoringDraft } from '@/apis'

export default defineComponent({
  props: {
    draft: {
      type: Object as () => RefactoringDraft,
      required: true,
    },
    category: {
      type: String as () => DiffCategory,
      required: true,
    },
  },
  setup(props) {
    const {
      app: { $accessor },
    } = useContext()

    const title = capitalize(props.category)
    const elementMetadataMap = computed(() => {
      const type = $accessor.draft.refactoringTypes.find(
        (t) => t.name === props.draft.type
      )
      return type ? type[props.category] : {}
    })
    const elementHolderMap = computed(() => {
      const entries = Object.entries(props.draft.data[props.category])
      const map = elementMetadataMap.value
      entries.sort((a, b) => {
        if (a[0] in map && b[0] in map) {
          if (map[a[0]].required && !map[b[0]].required) return -1
          if (!map[a[0]].required && map[b[0]].required) return 1
        } else if (a[0] in map) return -1
        else if (b[0] in map) return 1
        return a[0] < b[0] ? -1 : a[0] > b[0] ? 1 : 0
      })
      return Object.fromEntries(entries)
    })
    const isRemovable = (key: string) =>
      !Object.keys(elementMetadataMap.value).includes(key)

    return {
      title,
      elementHolderMap,
      elementMetadataMap,
      isRemovable,
    }
  },
})
</script>

<style lang="scss" scoped>
.list-container {
  min-height: 0;
  overflow-y: scroll;
}
</style>
