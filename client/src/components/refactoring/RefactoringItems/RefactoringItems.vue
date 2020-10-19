<template>
  <div>
    <refactoring-item
      v-for="refactoring in refactorings"
      :key="refactoring.id"
      :refactoring="refactoring"
      class="my-4"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent } from '@nuxtjs/composition-api'
import { Refactoring } from '@/apis'

export default defineComponent({
  props: {
    refactorings: {
      type: Array as () => Refactoring[],
      required: true,
    },
  },
})
</script>
