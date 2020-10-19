<template>
  <v-container>
    <v-row>
      <v-col>
        <h1>All Refactorings</h1>
        <v-divider class="mb-5" />
        <refactoring-items :refactorings="refactorings" />
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import { defineComponent, ref, useAsync } from '@nuxtjs/composition-api'
import apis, { Refactoring } from '@/apis'

export default defineComponent({
  name: 'all',
  setup() {
    const refactorings = ref<Refactoring[]>([])

    useAsync(async () => {
      refactorings.value = (await apis.refactorings.getAllRefactorings()).data
    })

    return { refactorings }
  },
})
</script>
