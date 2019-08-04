<template>
  <v-layout column justify-center align-center>
    <v-flex xs12 sm8 md6>
      {{ message }}
    </v-flex>
    <v-btn rounded @click="getHello">get</v-btn>
    <v-btn rounded @click="postHello">post</v-btn>
  </v-layout>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator'

@Component({
  async asyncData({ $axios }) {
    const { data } = await $axios.get('/api/hello')
    return { message: data }
  }
})
export default class extends Vue {
  message: string = ''
  private getHello(): void {
    this.$axios.get('/api/hello').then(response => {
      this.message = response.data
    })
  }
  private postHello(): void {
    this.$axios.post('/api/hello').then(response => {
      this.message = response.data
    })
  }
}
</script>
