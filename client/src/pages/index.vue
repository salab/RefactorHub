<template>
  <v-container>
    <div class="pa-5 text-center">
      <h1 class="display-3">RefactorHub</h1>
    </div>
    <v-divider />
    <v-row justify="center" class="pa-5">
      <div v-for="link in links" :key="link.to" class="ma-3">
        <v-btn :to="link.to" class="text-none">
          {{ link.name }}
        </v-btn>
      </div>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import { defineComponent } from '@nuxtjs/composition-api'

const links = [
  { name: 'All Refactorings', to: '/all' },
  { name: 'Experiment (type)', to: '/experiment/1' },
  { name: 'Experiment (description)', to: '/experiment/2' },
]

export default defineComponent({
  name: 'home',
  setup() {
    return { links }
  },
})
</script>
