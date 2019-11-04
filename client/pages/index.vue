<template>
  <v-container fluid fill-height>
    <v-row>
      <v-col>
        <v-row align="center" justify="center">
          <v-btn
            v-for="refactoring in refactorings"
            :key="refactoring.id"
            class="mx-2"
            @click="fork(refactoring.id)"
          >
            Fork ({{ refactoring.id }})
          </v-btn>
        </v-row>
        <v-divider class="my-4" />
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
import { Refactoring } from 'refactorhub'

@Component
export default class extends Vue {
  refactorings: Refactoring[] = []

  async mounted() {
    this.refactorings = await this.$client.getRefactorings()
  }

  async fork(id: number) {
    const draft = await this.$client.forkRefactoring(id)
    this.$router.push(`/draft/${draft.id}`)
  }
}
</script>
