<template>
  <v-app>
    <v-app-bar app flat>
      <v-container class="py-0 fill-height">
        <a href="/" class="d-inline-block">
          <v-img src="/icon.png" width="32" height="32"></v-img>
        </a>
        <v-spacer />
        <v-btn v-if="!isAuthenticated" href="/login" depressed>
          <v-icon left>fab fa-github</v-icon>
          Sign in
        </v-btn>
      </v-container>
    </v-app-bar>
    <v-main>
      <nuxt />
    </v-main>
  </v-app>
</template>

<script lang="ts">
import { computed, defineComponent, useContext } from '@nuxtjs/composition-api'

export default defineComponent({
  setup() {
    const {
      app: { $accessor },
    } = useContext()
    const isAuthenticated = computed(() => $accessor.isAuthenticated)
    return {
      isAuthenticated,
    }
  },
})
</script>
