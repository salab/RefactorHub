<script setup lang="ts">
import { NuxtError } from '@nuxt/types'

defineProps({
  error: {
    type: Object as () => NuxtError,
    required: true,
  },
})
</script>

<template>
  <v-container fluid fill-height>
    <v-row>
      <v-col>
        <v-row justify="center">
          <p class="display-4">
            {{ error.statusCode }}
          </p>
        </v-row>
        <v-row justify="center">
          <p class="headline">
            {{ error.message }}
          </p>
        </v-row>
      </v-col>
    </v-row>
  </v-container>
</template>
