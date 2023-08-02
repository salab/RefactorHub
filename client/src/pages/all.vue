<script setup lang="ts">
import apis, { Refactoring } from '@/apis'

const refactorings = ref<Refactoring[]>([])
useAsyncData(async () => {
  refactorings.value = (await apis.refactorings.getAllRefactorings()).data
})
</script>

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
