<template>
  <v-container fluid fill-height>
    <v-row align="center" justify="center">
      <v-col cols="12">
        <v-row align="center" justify="center">
          <v-btn to="hello" class="mx-2">
            Hello
          </v-btn>
        </v-row>
      </v-col>
      <v-col cols="12">
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
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator'
import { Refactoring, Draft } from 'refactorhub'

@Component
export default class extends Vue {
  refactorings: Refactoring[] = []

  async mounted() {
    const { data } = await this.$axios.get<Refactoring[]>('/api/refactoring')
    return (this.refactorings = data)
  }

  async fork(id: number) {
    const draft = (await this.$axios.post<Draft>(`/api/refactoring/${id}/fork`))
      .data
    this.$router.push(`/draft/${draft.id}`)
  }
}
</script>
