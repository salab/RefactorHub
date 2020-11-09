<template>
  <v-card
    outlined
    tile
    :width="20"
    class="d-flex justify-center overflow-x-scroll"
  >
    <span class="number-letter">{{ number }}</span>
  </v-card>
</template>

<script lang="ts">
import { defineComponent } from '@nuxtjs/composition-api'

export default defineComponent({
  props: {
    number: {
      type: Number,
    },
  },
})
</script>

<style lang="scss" scoped>
.overflow-x-scroll {
  overflow-x: scroll;
  scrollbar-width: none;
  &::-webkit-scrollbar {
    display: none;
  }
}

.number-letter {
  font-size: 0.4rem;
  font-family: 'Roboto', sans-serif;
  letter-spacing: -0.01rem;
}
</style>
