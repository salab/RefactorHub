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

<script lang="ts">
import { Component, Vue, Prop } from 'nuxt-property-decorator'
import { NuxtError } from '@nuxt/types'

@Component
export default class extends Vue {
  @Prop({ required: true })
  error!: NuxtError
}
</script>
