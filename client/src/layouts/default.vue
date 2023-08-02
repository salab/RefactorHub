<script setup lang="ts">
import { mdiGithub } from '@mdi/js'
const isAuthenticated = useUser().isAuthenticated
</script>

<template>
  <v-app>
    <v-app-bar color="grey-lighten-4" app flat>
      <v-container class="py-0 fill-height">
        <a href="/" class="d-inline-block">
          <v-img src="/icon.png" width="32" height="32"></v-img>
        </a>
        <v-spacer />
        <v-btn v-if="!isAuthenticated" href="/login" depressed>
          <v-icon :icon="mdiGithub" size="30" start />
          Sign in
        </v-btn>
      </v-container>
    </v-app-bar>
    <v-main>
      <slot />
    </v-main>
  </v-app>
</template>
