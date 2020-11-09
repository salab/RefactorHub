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
          <element-key-put-button :category="category" />
        </div>
      </div>
    </v-navigation-drawer>
  </v-card>
</template>

<script lang="ts">
import {
  defineComponent,
  computed,
  ref,
  watch,
  useContext,
} from '@nuxtjs/composition-api'
import { capitalize } from 'lodash-es'
import { DiffCategory } from 'refactorhub'
import apis, { RefactoringType } from '@/apis'

export default defineComponent({
  props: {
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

    const draft = computed(() => $accessor.draft.draft)
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
        type.value = (
          await apis.refactoringTypes.getRefactoringType(draft.value.type)
        ).data
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
