<template>
  <v-container fluid fill-height>
    <v-row align="center" justify="center">
      <v-col cols="12">
        <v-row align="center" justify="center">
          <v-card>
            <v-card-text>
              {{ message }}
            </v-card-text>
          </v-card>
        </v-row>
      </v-col>
      <v-col cols="12">
        <v-row align="center" justify="center">
          <v-btn class="mx-2" @click="getHello">get</v-btn>
          <v-btn class="mx-2" @click="postHello">post</v-btn>
          <v-btn class="mx-2" @click="getDraft">draft</v-btn>
        </v-row>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator'
import { Draft } from 'refactorhub'

@Component({
  async asyncData({ $axios }) {
    const { data } = await $axios.get<string>('/api/hello')
    return { message: data }
  }
})
export default class extends Vue {
  message: string = ''
  private async getHello() {
    try {
      this.message = (await this.$axios.get<string>('/api/hello')).data
    } catch (e) {
      this.message = e.message
    }
  }
  private async postHello() {
    try {
      this.message = (await this.$axios.post<string>('/api/hello')).data
    } catch (e) {
      this.message = e.message
    }
  }
  private async getDraft() {
    try {
      this.message = (await this.$axios.get<Draft>(
        '/api/draft/2'
      )).data.description
    } catch (e) {
      this.message = e.message
    }
  }
  private head() {
    return { title: 'Hello' }
  }
}
</script>
