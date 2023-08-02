<script setup lang="ts">
import { debounce } from 'lodash-es'
import apis, { CommitDetail, RefactoringDraft } from '@/apis'

const props = defineProps({
  draft: {
    type: Object as () => RefactoringDraft,
    required: true,
  },
  commit: {
    type: Object as () => CommitDetail,
    required: true,
  },
})

const draft = computed(() => props.draft)
const commit = computed(() => props.commit)
const refactoringTypes = computed(() => useDraft().refactoringTypes.value)
const messageLines = computed(() =>
  commit.value ? commit.value.message.split('\n') : [],
)

const updateDescription = debounce(async (value: string) => {
  useDraft().draft.value = (
    await apis.drafts.updateRefactoringDraft(draft.value.id, {
      description: value,
    })
  ).data
}, 500)
const updateRefactoringType = debounce(async (value: string) => {
  useDraft().draft.value = (
    await apis.drafts.updateRefactoringDraft(draft.value.id, {
      type: value,
    })
  ).data
}, 100)
</script>

<template>
  <v-expansion-panels>
    <v-expansion-panel :elevation="0" :rounded="0" class="info-panel">
      <v-expansion-panel-title v-slot="{ expanded: open }">
        <v-fade-transition>
          <div v-if="!open" class="d-flex">
            <div class="flex-grow-0 d-flex align-center pr-3">
              <h2>{{ draft.type }}</h2>
            </div>
            <div class="d-flex align-center">{{ draft.description }}</div>
          </div>
        </v-fade-transition>
      </v-expansion-panel-title>
      <v-expansion-panel-text>
        <v-container fluid class="py-0">
          <v-row>
            <v-col cols="6">
              <div>
                <v-select
                  v-if="refactoringTypes"
                  variant="underlined"
                  :model-value="draft.type"
                  :items="refactoringTypes.map((it) => it.name)"
                  label="Refactoring Type"
                  @update:model-value="updateRefactoringType"
                />
                <v-textarea
                  variant="underlined"
                  :model-value="draft.description"
                  rows="2"
                  label="Description"
                  @update:model-value="updateDescription"
                />
              </div>
            </v-col>
            <v-col cols="6">
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
                    'body-2': index > 0,
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
      </v-expansion-panel-text>
    </v-expansion-panel>
  </v-expansion-panels>
</template>

<style lang="scss" scoped>
.info-panel {
  ::v-deep(&) {
    .v-expansion-panel-text__wrap {
      padding: 0;
    }
    .v-expansion-panel-title {
      min-height: 2.4rem;
      padding: 0 0.8rem;
      .v-expansion-panel-title__icon {
        .v-icon {
          font-size: 1.2rem;
        }
      }
    }
  }
}
</style>
