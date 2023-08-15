<script setup lang="ts">
import { DiffCategory } from 'refactorhub'
import apis from '@/apis'
import { initElementDecorations } from '@/components/draft/ElementEditor/ts/elementDecorations'
import { initElementWidgets } from '@/components/draft/ElementEditor/ts/elementWidgets'
import { initCodeFragmentCursor } from '@/components/draft/ElementEditor/ts/codeFragments'

definePageMeta({
  layout: false,
  middleware: 'authenticated',
})

const paramId = useRoute().params.id
const draftId =
  typeof paramId === 'string' ? parseInt(paramId) : parseInt(paramId[0])
const router = useRouter()

const draft = computed(() => useDraft().draft.value)
const commit = computed(() => useDraft().commit.value)

initElementDecorations()
initElementWidgets()
initCodeFragmentCursor()
useDraft()
  .initStates(draftId)
  .then(() => {
    const { init } = useViewer()
    if (commit.value) init(commit.value)
  })

const isActiveOfElementHolders = reactive({
  before: true,
  after: true,
})

async function save() {
  const id = draft.value?.id
  if (id === undefined) return
  await apis.drafts.saveRefactoringDraft(id)
  router.back()
  // TODO: Jump preview page after save
  // const refactoring = (await apis.drafts.saveRefactoringDraft(id)).data
  // router.push(`/refactoring/${refactoring.id}`)
}
async function discard() {
  const id = draft.value?.id
  if (id === undefined) return
  await apis.drafts.discardRefactoringDraft(id)
  router.back()
}
</script>

<template>
  <v-app>
    <div class="app">
      <draft-action-bar
        :save="save"
        :discard="discard"
        :is-active-of-element-holders="{
          before: isActiveOfElementHolders.before,
          after: isActiveOfElementHolders.after,
        }"
        @toggle-element-holders="
          (category: DiffCategory) => {
            isActiveOfElementHolders[category] =
              !isActiveOfElementHolders[category]
          }
        "
      />
      <element-holders
        v-if="draft"
        :is-active="isActiveOfElementHolders.before"
        :draft="draft"
        category="before"
      />
      <element-holders
        v-if="draft"
        :is-active="isActiveOfElementHolders.after"
        :draft="draft"
        category="after"
      />
      <v-main
        v-if="draft && commit"
        class="d-flex flex-column fill-height pt-0"
      >
        <v-container
          fluid
          class="flex-grow-1 flex-shrink-0 d-flex min-height-0 pt-0 px-0"
        >
          <v-container
            fluid
            class="flex-grow-1 flex-shrink-0 d-flex flex-column pt-0 px-0"
          >
            <draft-summary :draft="draft" :commit="commit" />
            <v-divider />
            <div class="flex-grow-1 flex-shrink-0">
              <!-- <element-editor /> -->
              <main-viewers />
            </div>
            <v-divider />
            <commit-files :files="commit.files" />
          </v-container>
        </v-container>
        <element-type-colors />
      </v-main>
    </div>
  </v-app>
</template>

<style lang="scss" scoped>
.app {
  position: fixed;
  top: 0;
  right: 0;
  width: 100%;
  height: 100%;
}

.min-height-0 {
  min-height: 0;
}
</style>
