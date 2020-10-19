<template>
  <v-expansion-panels focusable flat tile>
    <v-expansion-panel class="info-panel">
      <v-expansion-panel-header v-slot="{ open }">
        <v-fade-transition>
          <div v-if="!open" class="d-flex">
            <div class="flex-grow-0 d-flex align-center pr-3 title">
              {{ draft.type }}
            </div>
            <div class="d-flex align-center">{{ draft.description }}</div>
          </div>
        </v-fade-transition>
      </v-expansion-panel-header>
      <v-expansion-panel-content>
        <v-container fluid class="py-0">
          <v-row>
            <v-col cols="6">
              <div>
                <v-select
                  v-if="refactoringTypes"
                  :value="draft.type"
                  :items="refactoringTypes.map((it) => it.name)"
                  label="Refactoring Type"
                  @input="updateRefactoringType"
                />
                <v-textarea
                  :value="draft.description"
                  rows="2"
                  label="Description"
                  @input="updateDescription"
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
      </v-expansion-panel-content>
    </v-expansion-panel>
  </v-expansion-panels>
</template>

<script lang="ts">
import { defineComponent, computed, useContext } from '@nuxtjs/composition-api'
import { debounce } from 'lodash-es'
import apis, { CommitDetail, RefactoringDraft } from '@/apis'

export default defineComponent({
  props: {
    draft: {
      type: Object as () => RefactoringDraft,
      required: true,
    },
    commit: {
      type: Object as () => CommitDetail,
      required: true,
    },
  },
  setup(props) {
    const {
      app: { $accessor },
    } = useContext()

    const draft = computed(() => props.draft)
    const commit = computed(() => props.commit)
    const refactoringTypes = computed(() => $accessor.draft.refactoringTypes)
    const messageLines = computed(() =>
      commit.value ? commit.value.message.split('\n') : []
    )

    const updateDescription = debounce(async (value: string) => {
      $accessor.draft.setDraft(
        (
          await apis.drafts.updateRefactoringDraft(draft.value.id, {
            description: value,
          })
        ).data
      )
    }, 500)
    const updateRefactoringType = debounce(async (value: string) => {
      $accessor.draft.setDraft(
        (
          await apis.drafts.updateRefactoringDraft(draft.value.id, {
            type: value,
          })
        ).data
      )
    }, 100)

    return {
      refactoringTypes,
      messageLines,
      updateDescription,
      updateRefactoringType,
    }
  },
})
</script>

<style lang="scss" scoped>
.info-panel {
  &::v-deep {
    .v-expansion-panel-content__wrap {
      padding: 0;
    }
    .v-expansion-panel-header {
      min-height: 2.4rem;
      padding: 0 0.8rem;
      .v-expansion-panel-header__icon {
        .v-icon {
          font-size: 1.2rem;
        }
      }
    }
  }
}
</style>
