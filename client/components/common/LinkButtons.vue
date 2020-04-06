<template>
  <v-row justify="center">
    <div v-for="link in links" :key="link.to" class="ma-2">
      <v-btn :to="link.to" class="text-none">
        {{ link.name }}
      </v-btn>
    </div>
  </v-row>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator'

@Component
export default class LinkButtons extends Vue {
  private links = [
    { name: 'Tutorial', to: '/tutorial' },
    { name: 'Experiment', to: '/experiment' },
  ]
}
</script>
