<template>
  <v-layout justify-center align-center>
    <v-flex v-if="error.statusCode === 404" display-1 text-xs-center>
      {{ pageNotFound }}
    </v-flex>
    <v-flex v-else display-1 text-xs-center>
      {{ otherError }}
    </v-flex>
  </v-layout>
</template>

<script lang="ts">
import { Component, Vue, Prop } from 'nuxt-property-decorator'

@Component({
  layout: 'empty'
})
export default class extends Vue {
  private head() {
    const title =
      this.error.statusCode === 404 ? this.pageNotFound : this.otherError
    return { title }
  }

  @Prop({ type: Object, required: true })
  error!: { statusCode: number }

  pageNotFound = '404 Not Found'
  otherError = 'An error occurred'
}
</script>
