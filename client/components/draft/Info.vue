<template>
  <v-expansion-panels focusable>
    <v-expansion-panel>
      <v-expansion-panel-header v-slot="{ open }" class="info-panel-header">
        <v-fade-transition>
          <v-flex v-if="draft && !open" d-flex flex-row>
            <v-flex flex-grow-0 d-flex align-center pr-3 title>
              {{ draft.type.name }}
            </v-flex>
            <v-flex d-flex align-center>{{ draft.description }}</v-flex>
          </v-flex>
        </v-fade-transition>
      </v-expansion-panel-header>
      <v-expansion-panel-content>
        <v-layout>
          <v-flex v-if="draft" xs6 px-3 pt-2>
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
import { Component, Vue } from 'nuxt-property-decorator'
import { Debounce } from 'lodash-decorators'

@Component
export default class Info extends Vue {
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