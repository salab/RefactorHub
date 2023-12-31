<script setup lang="ts">
import { annotationBarSize } from '@/components/annotation/AnnotationBar/AnnotationBar.vue'

definePageMeta({
  layout: false,
  middleware: 'authenticated',
})

const paramId = useRoute().params.id
const annotationId = typeof paramId === 'string' ? paramId : paramId[0]

const loadingIsStarted = ref(false)
const isLoading = useLoader().isLoading
useLoader()
  .startLoadingAnnotation(annotationId)
  .then(() => (loadingIsStarted.value = true))

const annotation = computed(() => useAnnotation().annotation.value)
const currentChange = computed(() => useAnnotation().currentChange.value)
</script>

<template>
  <v-app>
    <div v-if="isLoading">
      <loading-circle />
    </div>
    <div class="app">
      <annotation-bar v-if="loadingIsStarted && annotation" />
      <element-holders
        v-if="loadingIsStarted && annotation && currentChange"
        :current-change="currentChange"
        category="before"
      />
      <element-holders
        v-if="loadingIsStarted && annotation && currentChange"
        :current-change="currentChange"
        category="after"
      />
      <v-main
        v-if="loadingIsStarted && annotation"
        class="d-flex flex-column fill-height pt-0"
      >
        <v-container
          fluid
          class="flex-grow-1 flex-shrink-0 d-flex min-height-0 pt-0 px-0"
        >
          <v-container
            fluid
            class="flex-grow-1 flex-shrink-0 d-flex flex-column pt-0 px-0"
          >
            <div
              class="flex-grow-1 flex-shrink-0"
              :style="`margin-bottom: ${annotationBarSize - 35}px`"
            >
              <main-viewers v-if="currentChange" />
            </div>
          </v-container>
        </v-container>
        <annotation-colors />
      </v-main>
    </div>
  </v-app>
</template>

<style lang="scss" scoped>
.app {
  position: fixed;
  top: 0;
  right: 0;
  width: 100%;
  height: 100%;
}

.min-height-0 {
  min-height: 0;
}
</style>
