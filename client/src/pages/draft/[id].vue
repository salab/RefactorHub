<script setup lang="ts">
import { DiffCategory } from 'refactorhub'
import apis from '@/apis'

definePageMeta({
  layout: false,
  middleware: 'authenticated',
})

const paramId = useRoute().params.id
const draftId =
  typeof paramId === 'string' ? parseInt(paramId) : parseInt(paramId[0])

const loadingIsStarted = ref(false)
const isLoading = useLoader().isLoading
useLoader()
  .startLoading(draftId)
  .then(() => (loadingIsStarted.value = true))

const router = useRouter()

const draft = computed(() => useDraft().draft.value)
const commit = computed(() => useDraft().commit.value)

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
    <div v-if="isLoading">
      <loading-circle :active="isLoading" />
    </div>
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
        v-if="loadingIsStarted && draft"
        :is-active="isActiveOfElementHolders.before"
        :draft="draft"
        category="before"
      />
      <element-holders
        v-if="loadingIsStarted && draft"
        :is-active="isActiveOfElementHolders.after"
        :draft="draft"
        category="after"
      />
      <v-main
        v-if="loadingIsStarted && draft && commit"
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
              <main-viewers />
            </div>
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
