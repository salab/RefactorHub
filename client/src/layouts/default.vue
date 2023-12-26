<script setup lang="ts">
const isAuthenticated = useUser().isAuthenticated
</script>
<script lang="ts">
export const defaultAppBarHeight = 45
</script>

<template>
  <v-app>
    <v-app-bar color="primary" app flat :height="defaultAppBarHeight">
      <template #prepend>
        <a href="/" class="d-inline-block">
          <v-img src="/logo.png" width="128" />
        </a>
      </template>
      <template #append>
        <v-btn v-if="!isAuthenticated" href="/login" text="Sign in">
          <template #prepend>
            <v-icon icon="$mdiGithub" :size="30" />
          </template>
        </v-btn>
      </template>
    </v-app-bar>
    <v-main>
      <slot />
    </v-main>
  </v-app>
</template>
