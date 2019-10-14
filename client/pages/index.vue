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
            v-for="annot in annotations"
            :key="annot.id"
            class="mx-2"
            @click="fork(annot.id)"
          >
            Fork ({{ annot.id }})
          </v-btn>
        </v-row>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator'
import { Annotation, Draft } from '~/types'

@Component
export default class extends Vue {
  annotations: Annotation[] = []

  async mounted() {
    const { data } = await this.$axios.get<Annotation[]>('/api/annotation')
    return (this.annotations = data)
  }

  async fork(id: number) {
    const draft = (await this.$axios.post<Draft>(`/api/annotation/${id}/fork`))
      .data
    this.$router.push(`/draft/${draft.id}`)
  }
}
</script>
