<script setup lang="ts">
import { capitalize } from 'lodash-es'
import { DiffCategory } from 'refactorhub'
import { RefactoringDraft } from '@/apis'

const props = defineProps({
  draft: {
    type: Object as () => RefactoringDraft,
    required: true,
  },
  category: {
    type: String as () => DiffCategory,
    required: true,
  },
})

const title = capitalize(props.category)
const elementMetadataMap = computed(() => {
  const type = useDraft().refactoringTypes.value.find(
    (t) => t.name === props.draft.type,
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
</script>

<template>
  <v-navigation-drawer
    :location="category === 'after' ? 'right' : 'left'"
    :width="200"
    permanent
  >
    <div class="d-flex flex-column fill-height">
      <div class="d-flex justify-center py-1">
        <span class="font-weight-medium">{{ title }} Elements</span>
      </div>
      <v-divider />
      <div class="flex-grow-1 list-container">
        <v-list :opened="Object.keys(elementHolderMap)" class="py-0">
          <element-holder
            v-for="(holder, key) in elementHolderMap"
            :key="key"
            :draft-id="draft.id"
            :category="category"
            :element-key="key.toString()"
            :element-holder="holder"
            :element-metadata="elementMetadataMap[key]"
            :is-removable="isRemovable(key.toString())"
          />
        </v-list>
      </div>
      <div>
        <v-divider />
        <element-key-put-button :category="category" />
      </div>
    </div>
  </v-navigation-drawer>
</template>

<style lang="scss" scoped>
.list-container {
  min-height: 0;
  overflow-y: scroll;
}
</style>
