<script setup lang="ts">
import { debounce } from 'lodash-es'
import { CommonTokensType } from './ElementEditor/ts/commonTokensDecorations'
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

const commonTokensTypes = [
  'oneToOne',
  'oneToManyOrManyToOne',
  'ManyToMany',
] as CommonTokensType[]

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

const updateCommonTokensTypes = (types: CommonTokensType[]) => {
  // TODO: apply to the highlights
  console.log(types)
}
</script>

<template>
  <v-expansion-panels>
    <v-expansion-panel :elevation="0" :rounded="0" class="info-panel">
      <v-expansion-panel-title>
        <v-fade-transition>
          <div class="d-flex">
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
            <v-col>
              <h3>Commit Information</h3>
              <div class="text-subtitle-1 font-weight-medium">
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
                    'text-body-1 font-weight-medium': index === 0,
                    'text-body-2': index > 0,
                  }"
                >
                  {{ line }}
                </div>
              </div>
              <v-divider class="my-1" />
              <div>
                <span class="text-subtitle-2">{{ commit.author }}</span>
                <span class="text-body-2"> committed on </span>
                <span class="text-body-2">{{
                  new Date(commit.authorDate).toLocaleString()
                }}</span>
              </div>
            </v-col>
            <v-col>
              <h3>Annotation</h3>
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
            </v-col>
            <v-col>
              <h3>Editor Settings</h3>
              <h4>Common Tokens Highlights</h4>
              <p>
                3 types based on the occurrence in
                <b
                  ><span :style="'background-color: ' + colors.before"
                    >before code</span
                  >
                  to
                  <span :style="'background-color: ' + colors.after"
                    >after code</span
                  ></b
                >
              </p>
              <v-chip-group
                filter
                multiple
                @update:model-value="updateCommonTokensTypes"
              >
                <v-chip :value="commonTokensTypes[0]">one to one</v-chip>
                <v-chip :value="commonTokensTypes[1]"
                  >one to many / many to one</v-chip
                >
                <v-chip :value="commonTokensTypes[2]">many to many</v-chip>
              </v-chip-group>
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
