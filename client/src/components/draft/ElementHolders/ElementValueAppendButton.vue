<template>
  <v-btn text tile block small color="secondary" @click="addLocation">
    <span class="text-none body-2">Add Location</span>
  </v-btn>
</template>

<script lang="ts">
import { defineComponent, computed, useContext } from '@nuxtjs/composition-api'
import { DiffCategory } from 'refactorhub'
import apis from '@/apis'

export default defineComponent({
  props: {
    category: {
      type: String as () => DiffCategory,
      required: true,
    },
    elementKey: {
      type: String,
      required: true,
    },
  },
  setup(props) {
    const {
      app: { $accessor },
    } = useContext()

    const draft = computed(() => $accessor.draft.draft)
    const addLocation = async () => {
      if (draft.value) {
        await $accessor.draft.setDraft(
          (
            await apis.drafts.appendRefactoringDraftElementValue(
              draft.value.id,
              props.category,
              props.elementKey
            )
          ).data
        )
      }
    }
    return {
      addLocation,
    }
  },
})
</script>
