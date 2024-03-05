<script setup lang="ts">
const isAuthenticated = useUser().isAuthenticated
</script>

<template>
  <v-container>
    <div class="py-10 text-center">
      <v-img src="/logo.png" max-height="100" />
      <div class="mt-6 text-h5">A Commit Annotator for Refactoring</div>
      <div class="text-subtitle-1">Version 2.0.0-ss</div>
    </div>
    <v-divider />
    <div v-if="isAuthenticated" class="ma-6">
      <div class="d-flex justify-center">
        <v-btn to="/experiment" flat class="text-none mx-3">
          Experiments
        </v-btn>
        <v-btn to="/types" flat class="text-none mx-3">
          Refactoring Types
        </v-btn>
      </div>
    </div>
    <div v-else class="text-center ma-6">
      <div class="text-title-1">Please Sign-in to Continue</div>
      <v-btn href="/login" flat class="text-none mt-1">
        SIGN IN
        <template #prepend>
          <v-icon icon="$mdiGithub" :size="30" />
        </template>
      </v-btn>
    </div>
    <div class="text-center">
      <v-divider />
      <h2 class="text-h5 mt-6">Demonstration</h2>
      <iframe
        class="mt-4"
        width="560"
        height="315"
        src="https://www.youtube.com/embed/H2Blz8ovwe0"
        frameborder="0"
        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
        allowfullscreen
      />
    </div>
  </v-container>
</template>
