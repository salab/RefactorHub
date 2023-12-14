<script setup lang="ts">
const isAuthenticated = useUser().isAuthenticated
</script>

<template>
  <v-container>
    <div class="py-10 text-center">
      <v-img src="/logo.png" contain max-height="100"></v-img>
      <div class="mt-6 text-h5">A Commit Annotator for Refactoring</div>
    </div>
    <template v-if="isAuthenticated">
      <v-divider />
      <div class="d-flex justify-center pa-6">
        <v-btn to="/experiment" flat class="text-none mx-3">
          Experiments
        </v-btn>
        <v-btn to="/types" flat class="text-none mx-3">
          Refactoring Types
        </v-btn>
      </div>
    </template>
    <v-divider />
    <div class="py-8 text-center">
      <h2 class="text-h4">Demonstration</h2>
      <iframe
        class="mt-4"
        width="560"
        height="315"
        src="https://www.youtube.com/embed/Ew1wVBZkpro"
        frameborder="0"
        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
        allowfullscreen
      ></iframe>
    </div>
  </v-container>
</template>
