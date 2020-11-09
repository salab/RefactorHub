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
import apis, { RefactoringDraft } from '@/apis'

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
    const elementHolderMap = computed(() => props.draft.data[props.category])
    const elementMetadataMap = computed(() => {
      const type = $accessor.draft.refactoringTypes.find(
        (t) => t.name === props.draft.type
      )
      return type ? type[props.category] : {}
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
