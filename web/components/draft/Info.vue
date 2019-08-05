<template>
  <v-expansion-panels focusable>
    <v-expansion-panel class="radius-0">
      <v-expansion-panel-header class="min-height-0 px-4 py-2">
        <v-flex>
          <span class="title">Basic Info</span>
        </v-flex>
      </v-expansion-panel-header>
      <v-expansion-panel-content>
        Basic Infomation
      </v-expansion-panel-content>
    </v-expansion-panel>
  </v-expansion-panels>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator'

@Component
export default class Info extends Vue {}
</script>
