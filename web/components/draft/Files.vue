<template>
  <v-expansion-panels focusable>
    <v-expansion-panel class="radius-0">
      <v-expansion-panel-header class="min-height-0 pa-0 pr-2">
        <v-layout>
          <v-flex xs6 pl-4 py-2 mr-8>
            <span class="subtitle-1">src/before/File.java</span>
          </v-flex>
          <v-divider vertical />
          <v-flex xs6 pl-4 py-2>
            <span class="subtitle-1">src/after/File.java</span>
          </v-flex>
        </v-layout>
      </v-expansion-panel-header>
      <v-expansion-panel-content class="panel-pa-0">
        <v-layout>
          <v-flex xs6>
            Before Files
          </v-flex>
          <v-divider vertical />
          <v-flex xs6>
            After Files
          </v-flex>
        </v-layout>
      </v-expansion-panel-content>
    </v-expansion-panel>
  </v-expansion-panels>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator'

@Component
export default class Files extends Vue {}
</script>
