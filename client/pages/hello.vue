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

@Component({
  async asyncData({ app }) {
    const message = await app.$client.getHello()
    return { message }
  }
})
export default class extends Vue {
  message: string = ''
  private async getHello() {
    try {
      this.message = await this.$client.getHello()
    } catch (e) {
      if (e instanceof Error) this.message = e.message
    }
  }
  private async postHello() {
    try {
      this.message = await this.$client.postHello()
    } catch (e) {
      if (e instanceof Error) this.message = e.message
    }
  }
  private async getDraft() {
    try {
      this.message = (await this.$client.getDraft(1)).description
    } catch (e) {
      if (e instanceof Error) this.message = e.message
    }
  }
  private head() {
    return { title: 'Hello' }
  }
}
</script>
