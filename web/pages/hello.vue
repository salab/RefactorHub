<template>
  <v-layout column justify-center align-center>
    <v-flex xs12 sm8 md6>
      {{ message }}
    </v-flex>
    <v-btn rounded @click="getHello">get</v-btn>
    <v-btn rounded @click="postHello">post</v-btn>
    <v-btn rounded @click="getDraft">draft</v-btn>
  </v-layout>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator'
import { Draft } from '~/types'

@Component({
  async asyncData({ $axios }) {
    const { data } = await $axios.get<string>('/api/hello')
    return { message: data }
  }
})
export default class extends Vue {
  message: string = ''
  private getHello(): void {
    this.$axios.get<string>('/api/hello').then(response => {
      this.message = response.data
    })
  }
  private postHello(): void {
    this.$axios.post<string>('/api/hello').then(response => {
      this.message = response.data
    })
  }
  private getDraft(): void {
    this.$axios.get<Draft>('/api/draft/2').then(response => {
      this.message = response.data.description
    })
  }
  private head() {
    return { title: 'Hello' }
  }
}
</script>
