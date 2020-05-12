<template>
  <v-expansion-panels focusable flat tile>
    <v-expansion-panel class="info-panel">
      <v-expansion-panel-header v-slot="{ open }">
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
              <div>
                <v-select
                  v-if="refactoringTypes"
                  :value="draft.type.name"
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
            <v-col v-if="commitInfo" cols="6">
              <div class="subtitle-1 font-weight-medium">
                <span>{{ commitInfo.owner }}</span>
                /
                <span>{{ commitInfo.repository }}</span>
                /
                <a :href="commitInfo.url" target="_blank" rel="noopener">{{
                  commitInfo.sha.substring(0, 7)
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
                <span class="subtitle-2">{{ commitInfo.author }}</span>
                <span class="body-2">committed on</span>
                <span class="body-2">{{
                  new Date(commitInfo.authorDate).toLocaleString()
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
import { defineComponent, computed } from '@vue/composition-api'
import { debounce } from 'lodash'

export default defineComponent({
  name: 'DraftInfo',
  setup(_, { root }) {
    const draft = computed(() => root.$accessor.draft.draft)
    const commitInfo = computed(() => root.$accessor.draft.commitInfo)
    const refactoringTypes = computed(
      () => root.$accessor.draft.refactoringTypes
    )
    const messageLines = computed(() =>
      commitInfo.value ? commitInfo.value.message.split('\n') : []
    )

    return {
      draft,
      commitInfo,
      refactoringTypes,
      messageLines,
      updateDescription: debounce(async (value: string) => {
        if (!draft.value) return
        root.$accessor.draft.setDraft(
          await root.$client.updateDraft(draft.value.id, { description: value })
        )
      }, 500),
      updateRefactoringType: debounce(async (value: string) => {
        if (!draft.value) return
        root.$accessor.draft.setDraft(
          await root.$client.updateDraft(draft.value.id, { type: value })
        )
      }, 100),
    }
  },
})
</script>

<style lang="scss" scoped>
.info-panel ::v-deep .v-expansion-panel-content__wrap {
  padding: 0;
}

.info-panel ::v-deep .v-expansion-panel-header {
  min-height: 36px;
  padding: 0 12px;
}
</style>
