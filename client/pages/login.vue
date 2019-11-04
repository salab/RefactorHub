<template>
  <v-container fluid fill-height>
    <v-row>
      <v-col>
        <v-row align="center" justify="center">
          <v-btn @click="$auth.loginWith('github')">
            <v-icon left>fab fa-github</v-icon> Login
          </v-btn>
        </v-row>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator'

@Component
export default class extends Vue {}
</script>
