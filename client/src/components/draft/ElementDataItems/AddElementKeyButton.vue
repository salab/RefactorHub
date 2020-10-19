<template>
  <div>
    <div class="px-3 pb-1">
      <v-text-field
        v-model="elementKey"
        label="elementName"
        dense
        single-line
        hide-details
      />
      <v-select
        v-model="elementType"
        :items="elementTypes"
        label="elementType"
        dense
        single-line
        hide-details
      />
      <v-checkbox
        v-model="multiple"
        label="multiple"
        dense
        hide-details
        class="mt-1"
      />
    </div>
    <v-divider />
    <v-btn text tile block small color="secondary" @click="addElementKey">
      <span class="text-none body-1">Add Element</span>
    </v-btn>
  </div>
</template>

<script lang="ts">
import { defineComponent, computed, ref } from '@nuxtjs/composition-api'
import { DiffCategory } from 'refactorhub'
import apis from '@/apis'

export default defineComponent({
  name: 'AddElementKeyButton',
  props: {
    category: {
      type: String as () => DiffCategory,
      required: true,
    },
  },
  setup(props, { root }) {
    const draft = computed(() => root.$accessor.draft.draft)
    const elementTypes = computed(() => root.$accessor.draft.elementTypes)

    const elementKey = ref('')
    const elementType = ref('')
    const multiple = ref(false)

    const addElementKey = async () => {
      if (draft.value && elementKey.value && elementType.value) {
        await root.$accessor.draft.setDraft(
          // TODO: error handling
          (
            await apis.drafts.putRefactoringDraftElementKey(
              draft.value.id,
              props.category,
              {
                key: elementKey.value,
                type: elementType.value,
                multiple: multiple.value,
              }
            )
          ).data
        )
        elementKey.value = ''
        elementType.value = ''
      }
    }

    return {
      elementTypes,
      elementKey,
      elementType,
      multiple,
      addElementKey,
    }
  },
})
</script>
