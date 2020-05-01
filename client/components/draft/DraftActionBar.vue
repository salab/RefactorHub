<template>
  <v-app-bar dense>
    <v-spacer />
    <v-btn small color="primary" class="ml-2" @click="save">
      Save
    </v-btn>
    <v-btn small class="ml-2" @click="cancel">
      Cancel
    </v-btn>
  </v-app-bar>
</template>

<script lang="ts">
import { defineComponent } from '@vue/composition-api'

export default defineComponent({
  name: 'DraftActionBar',
  props: {
    id: {
      type: Number,
      required: true,
    },
  },
  setup(props, { root }) {
    const save = async () => {
      await root.$client.saveDraft(props.id)
      root.$router.back()
      // TODO: jump preview page after save
      // const refactoring = await root.$client.saveDraft(props.id)
      // root.$router.push(`/refactoring/${refactoring.id}`)
    }
    const cancel = async () => {
      await root.$client.cancelDraft(props.id)
      root.$router.back()
    }
    return { save, cancel }
  },
})
</script>
