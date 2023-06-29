<template>
  <v-container>
    <div class="py-3">
      <h1 class="text-h4">All Refactorings</h1>
    </div>
    <v-divider />
    <div class="py-2">
      <refactoring-items :refactorings="refactorings" />
    </div>
  </v-container>
</template>

<script lang="ts">
import { defineComponent, ref, useAsync } from '@nuxtjs/composition-api'
import apis, { Refactoring } from '@/apis'

export default defineComponent({
  setup() {
    const refactorings = ref<Refactoring[]>([])

    useAsync(async () => {
      refactorings.value = (await apis.refactorings.getAllRefactorings()).data
    }, 'manualKey')

    return { refactorings }
  },
})
</script>
