<template>
  <v-btn text tile block small color="secondary" @click="addLocation">
    <span class="text-none body-2">Add Location</span>
  </v-btn>
</template>

<script lang="ts">
import { defineComponent, computed } from '@nuxtjs/composition-api'
import { DiffCategory } from 'refactorhub'

export default defineComponent({
  name: 'AddLocationButton',
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
  setup(props, { root }) {
    const draft = computed(() => root.$accessor.draft.draft)
    const addLocation = async () => {
      if (draft.value) {
        await root.$accessor.draft.setDraft(
          await root.$client.appendElementValue(
            draft.value.id,
            props.category,
            props.elementKey
          )
        )
      }
    }
    return {
      addLocation,
    }
  },
})
</script>
