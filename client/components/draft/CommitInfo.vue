<template>
  <v-expansion-panels focusable tile>
    <v-expansion-panel>
      <v-expansion-panel-header v-slot="{ open }" class="info-panel-header">
        <v-fade-transition>
          <div v-if="draft && !open" class="d-flex">
            <div class="flex-grow-0 d-flex align-center pr-3 title">
              {{ draft.type.name }}
            </div>
            <div class="d-flex align-center">{{ draft.description }}</div>
          </div>
        </v-fade-transition>
      </v-expansion-panel-header>
      <v-expansion-panel-content>
        <v-container fluid class="py-0">
          <v-row>
            <v-col v-if="draft" cols="6">
              <form @submit.prevent="">
                <v-select
                  v-if="refactoringTypes"
                  :value="draft.type.name"
                  :items="refactoringTypes.map(it => it.name)"
                  label="Refactoring Type"
                  @input="onInputType"
                />
                <v-textarea
                  :value="draft.description"
                  rows="2"
                  label="Description"
                  @input="onInputDescription"
                />
              </form>
            </v-col>
            <v-col v-if="commit" cols="6">
              <div class="subtitle-1 font-weight-medium">
                <span>{{ commit.owner }}</span>
                /
                <span>{{ commit.repository }}</span>
                /
                <a :href="commit.url" target="_blank" rel="noopener">{{
                  commit.sha.substring(0, 7)
                }}</a>
              </div>
              <v-divider class="my-1" />
              <div>
                <div
                  v-for="(line, index) in messageLines"
                  :key="index"
                  :class="{
                    'body-1 font-weight-medium': index === 0,
                    'body-2': index > 0
                  }"
                >
                  {{ line }}
                </div>
              </div>
              <v-divider class="my-1" />
              <div>
                <span class="subtitle-2">{{ commit.author }}</span>
                <span class="body-2">committed on</span>
                <span class="body-2">{{
                  new Date(commit.authorDate).toLocaleString()
                }}</span>
              </div>
            </v-col>
          </v-row>
        </v-container>
      </v-expansion-panel-content>
    </v-expansion-panel>
  </v-expansion-panels>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator'
import { Debounce } from 'lodash-decorators'

@Component
export default class CommitInfo extends Vue {
  private get draft() {
    return this.$accessor.draft.draft
  }

  private get commit() {
    return this.$accessor.draft.commit
  }

  private get refactoringTypes() {
    return this.$accessor.draft.refactoringTypes
  }

  private get messageLines(): string[] {
    if (!this.commit) return []
    return this.commit.message.split('\n')
  }

  @Debounce(500)
  private async onInputDescription(value: string) {
    if (!this.draft) return
    this.$accessor.draft.setDraft(
      await this.$client.updateDraft(this.draft.id, value)
    )
  }

  @Debounce(100)
  private async onInputType(value: string) {
    if (!this.draft) return
    this.$accessor.draft.setDraft(
      await this.$client.updateDraft(this.draft.id, undefined, value)
    )
  }
}
</script>

<style lang="scss" scope>
.info-panel-header {
  min-height: 36px !important;
  padding: 0 12px;
}
</style>
