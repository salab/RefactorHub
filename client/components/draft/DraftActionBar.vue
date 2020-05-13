<template>
  <v-card flat tile>
    <v-app-bar dense flat>
      <v-spacer />
      <v-btn depressed small tile color="primary" class="ml-2" @click="save">
        save
      </v-btn>
      <v-btn depressed small tile text class="ml-2" @click="cancel">
        cancel
      </v-btn>
    </v-app-bar>
  </v-card>
</template>

<script lang="ts">
import { defineComponent, computed } from '@vue/composition-api'

export default defineComponent({
  name: 'DraftActionBar',
  setup(_, { root }) {
    const id = computed(() => root.$accessor.draft.draft?.id)

    const save = async () => {
      if (!id.value) return
      await root.$client.saveDraft(id.value)
      root.$router.back()
      // TODO: jump preview page after save
      // const refactoring = await root.$client.saveDraft(props.id)
      // root.$router.push(`/refactoring/${refactoring.id}`)
    }
    const cancel = async () => {
      if (!id.value) return
      await root.$client.cancelDraft(id.value)
      root.$router.back()
    }
    return { save, cancel }
  },
})
</script>
