<template>
  <v-container fill-height>
    <v-col>
      <v-row>
        <div class="headline">Tutorials</div>
      </v-row>
      <v-row>
        <v-col v-for="tutorial in tutorials" :key="tutorial.id" cols="12">
          <v-row>
            <v-divider class="mb-4" />
          </v-row>
          <v-row align="center">
            <div class="title">
              {{ tutorial.type.name }}(id={{ tutorial.id }})
            </div>
            <v-btn class="text-none ml-2" @click="fork(tutorial.id)">
              <v-icon left>fa-code-branch</v-icon>
              Fork
            </v-btn>
          </v-row>
          <v-row>
            <div class="subtitle-1">My Refactorings</div>
          </v-row>
          <v-row>
            <div
              v-for="refactoring in getRefactorings(tutorial.id)"
              :key="refactoring.id"
              class="ma-2"
            >
              <v-btn class="text-none" @click="edit(refactoring.id)">
                {{ refactoring.type.name }}(id={{ refactoring.id
                }}{{
                  refactoring.parent ? `, parent=${refactoring.parent.id}` : ''
                }})
              </v-btn>
            </div>
          </v-row>
          <v-row>
            <div class="subtitle-1">My Drafts</div>
          </v-row>
          <v-row>
            <div
              v-for="draft in getDrafts(tutorial.id)"
              :key="draft.id"
              class="ma-2"
            >
              <v-btn class="text-none" @click="open(draft.id)">
                {{ draft.type.name }}(id={{ draft.id
                }}{{ draft.parent ? `, parent=${draft.parent.id}` : ''
                }}{{ draft.origin ? `, origin=${draft.origin.id}` : '' }})
              </v-btn>
            </div>
          </v-row>
        </v-col>
      </v-row>
    </v-col>
  </v-container>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator'
import { Refactoring, Draft, User } from 'refactorhub'

@Component
export default class extends Vue {
  private tutorials: Refactoring[] = []
  private refactorings: Refactoring[] = []
  private drafts: Draft[] = []

  private async mounted() {
    this.tutorials = await this.$client.getUserRefactorings(1)
    const user = this.$auth.user as User
    this.refactorings = await this.$client.getUserRefactorings(user.id)
    this.drafts = await this.$client.getUserDrafts(user.id)
  }

  private async fork(id: number) {
    const draft = await this.$client.forkRefactoring(id)
    this.$router.push(`/draft/${draft.id}`)
  }

  private async edit(id: number) {
    const draft = await this.$client.editRefactoring(id)
    this.$router.push(`/draft/${draft.id}`)
  }

  private open(id: number) {
    this.$router.push(`/draft/${id}`)
  }

  private getRefactorings(id: number) {
    return this.refactorings.filter(it => it.parent?.id === id)
  }

  private getDrafts(id: number) {
    return this.drafts.filter(it => it.parent?.id === id)
  }
}
</script>
