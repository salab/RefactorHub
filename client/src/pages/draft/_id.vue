<template>
  <div class="app">
    <div v-if="draft && commit" class="d-flex flex-column fill-height">
      <draft-action-bar :save="save" :discard="discard" />
      <v-divider />
      <draft-summary :draft="draft" :commit="commit" />
      <v-divider />
      <div class="flex-grow-1 d-flex min-height-0">
        <element-holders :draft="draft" category="before" />
        <div class="flex-grow-1 d-flex flex-column">
          <div class="flex-grow-1">
            <element-editor />
          </div>
          <v-divider />
          <commit-files :files="commit.files" />
        </div>
        <element-holders :draft="draft" category="after" />
      </div>
      <element-type-colors />
    </div>
  </div>
</template>

<script lang="ts">
import {
  computed,
  defineComponent,
  useAsync,
  useContext,
} from '@nuxtjs/composition-api'
import apis from '@/apis'
import { initElementDecorations } from '@/components/draft/ElementEditor/ts/elementDecorations'
import { initElementWidgets } from '@/components/draft/ElementEditor/ts/elementWidgets'
import { initCodeFragmentCursor } from '@/components/draft/ElementEditor/ts/codeFragments'

export default defineComponent({
  middleware: 'authenticated',
  setup() {
    const {
      params,
      app: { router, $accessor },
    } = useContext()
    const draft = computed(() => $accessor.draft.draft)
    const commit = computed(() => $accessor.draft.commit)

    useAsync(async () => {
      initElementDecorations()
      initElementWidgets()
      initCodeFragmentCursor()
      await $accessor.draft.initStates(parseInt(params.value.id))
    })

    async function save() {
      const id = draft.value?.id
      if (id === undefined) return
      await apis.drafts.saveRefactoringDraft(id)
      router?.back()
      // TODO: Jump preview page after save
      // const refactoring = (await apis.drafts.saveRefactoringDraft(id)).data
      // router?.push(`/refactoring/${refactoring.id}`)
    }
    async function discard() {
      const id = draft.value?.id
      if (id === undefined) return
      await apis.drafts.discardRefactoringDraft(id)
      router?.back()
    }

    return {
      draft,
      commit,
      save,
      discard,
    }
  },
})
</script>

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
