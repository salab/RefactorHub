<template>
  <div
    v-if="useLoader().isLoading"
    class="background d-flex flex-column align-center justify-center"
  >
    <v-progress-circular indeterminate />
    <span class="text-caption">{{ useLoader().loadingText.value }}</span>
  </div>
</template>

<style lang="scss" scoped>
.background {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  background: white;
  opacity: 0.5;
  z-index: 9999;
}
</style>
