<script setup lang="ts">
defineProps({
  number: {
    type: Number,
    default: null,
  },
})
</script>

<template>
  <v-card
    variant="outlined"
    color="grey"
    :rounded="0"
    :width="24"
    class="d-flex justify-center overflow-x-scroll"
  >
    <span class="number-letter" style="color: black">{{ number }}</span>
  </v-card>
</template>

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
