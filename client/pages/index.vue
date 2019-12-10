<template>
  <v-container fluid fill-height>
    <v-row>
      <v-col>
        <v-row align="center" justify="center">
          <div v-if="$auth.loggedIn">Welcome, {{ $auth.user.name }}</div>
          <v-btn v-else @click="$auth.loginWith('github')">
            <v-icon left>fab fa-github</v-icon> Login
          </v-btn>
        </v-row>
        <v-divider class="my-4" />
        <v-row align="center" justify="center">
          <p class="subtitle-1">Tutorials</p>
        </v-row>
        <v-row align="center" justify="center">
          <v-btn
            v-for="refactoring in tutorials"
            :key="refactoring.id"
            class="text-capitalize mx-2"
            @click="fork(refactoring.id)"
          >
            {{ refactoring.type.name }} ({{ refactoring.id }})
          </v-btn>
        </v-row>
        <v-divider class="my-4" />
        <v-row align="center" justify="center">
          <p class="subtitle-1">Experiments</p>
        </v-row>
        <v-row align="center" justify="center">
          <v-btn
            v-for="refactoring in experiments"
            :key="refactoring.id"
            class="text-capitalize mx-2"
            @click="fork(refactoring.id)"
          >
            {{ refactoring.type.name }} ({{ refactoring.id }})
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
  tutorials: Refactoring[] = []
  experiments: Refactoring[] = []

  async mounted() {
    this.tutorials = await this.$client.getUserRefactorings(1)
    this.experiments = await this.$client.getUserRefactorings(2)
  }

  async fork(id: number) {
    const draft = await this.$client.forkRefactoring(id)
    this.$router.push(`/draft/${draft.id}`)
  }
}
</script>
