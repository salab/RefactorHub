<template>
  <div>
    <v-divider />
    <v-btn text tile block small color="secondary" @click="addLocation">
      <span class="text-none body-2">Add Location</span>
    </v-btn>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType, computed } from '@vue/composition-api'
import { DiffCategory } from 'refactorhub'

export default defineComponent({
  name: 'AddLocationButton',
  props: {
    category: {
      type: String as PropType<DiffCategory>,
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
          await root.$client.addElementLocation(
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
