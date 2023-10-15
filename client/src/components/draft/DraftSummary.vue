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
const refactoringTypes = computed(() => useDraft().refactoringTypes.value)

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
      <v-expansion-panel-title>
        <div class="d-flex">
          <div class="flex-grow-0 d-flex align-center pr-3">
            <h2>{{ draft.type }}</h2>
          </div>
          <div class="d-flex align-center">{{ draft.description }}</div>
        </div>
      </v-expansion-panel-title>
      <v-expansion-panel-text>
        <v-container fluid class="pa-0">
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
