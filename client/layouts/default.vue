<template>
  <v-app>
    <v-content>
      <nuxt />
    </v-content>
  </v-app>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator'

@Component
export default class extends Vue {}
</script>
