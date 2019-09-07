<template>
  <v-expansion-panels focusable>
    <v-expansion-panel class="radius-0">
      <v-expansion-panel-header
        v-slot="{ open }"
        class="min-height-0 px-4 py-2"
      >
        <v-flex flex-grow-0 pr-5 title>Basic Info</v-flex>
        <v-fade-transition>
          <v-flex v-if="!open" d-flex flex-row text--secondary>
            <v-flex flex-grow-0 pr-3 font-weight-bold>{{ draft.type }}</v-flex>
            <v-flex>{{ draft.description }}</v-flex>
          </v-flex>
        </v-fade-transition>
      </v-expansion-panel-header>
      <v-expansion-panel-content>
        <v-layout>
          <v-flex xs6 px-3 pt-2>
            <form @submit.prevent="">
              <v-select
                v-model="draft.type"
                :items="refactoringTypes"
                label="Refactoring Type"
              />
              <v-textarea
                v-model="draft.description"
                rows="2"
                label="Description"
              />
            </form>
          </v-flex>
          <v-divider vertical />
          <v-flex v-if="commit" xs6 px-3 py-1>
            <v-flex py-1>
              <span class="subtitle-1 font-weight-medium">{{
                commit.owner
              }}</span>
              /
              <span class="subtitle-1 font-weight-medium">{{
                commit.repository
              }}</span>
              /
              <a
                :href="commit.url"
                class="subtitle-1 font-weight-medium"
                target="_blank"
                rel="noopener"
                >{{ commit.sha.substring(0, 7) }}</a
              >
            </v-flex>
            <v-divider />
            <v-flex py-1>
              <v-flex
                v-for="(line, index) in messageLines"
                :key="index"
                :class="{
                  'body-1 font-weight-medium': index === 0,
                  'body-2': index > 0
                }"
              >
                {{ line }}
              </v-flex>
            </v-flex>
            <v-divider />
            <v-flex py-1>
              <span class="subtitle-2">{{ commit.author }}</span>
              <span class="body-2">committed on</span>
              <span class="body-2">{{
                new Date(commit.authorDate).toLocaleString()
              }}</span>
            </v-flex>
          </v-flex>
        </v-layout>
      </v-expansion-panel-content>
    </v-expansion-panel>
  </v-expansion-panels>
</template>

<script lang="ts">
import { Component, Vue, Prop } from 'nuxt-property-decorator'
import { Draft, CommitInfo } from '~/types'

@Component
export default class Info extends Vue {
  @Prop({ required: true })
  private draft!: Draft
  private commit: CommitInfo | null = null
  private refactoringTypes: string[] = []

  private get messageLines(): string[] {
    if (!this.commit) return []
    return this.commit.message.split('\n')
  }

  private async created() {
    {
      const { data } = await this.$axios.get<CommitInfo>(
        `/api/commit/${this.draft.commit.sha}/info`
      )
      this.commit = data
    }
    {
      const { data } = await this.$axios.get<string[]>('/api/refactoring/types')
      this.refactoringTypes.push(...data)
    }
  }
}
</script>
