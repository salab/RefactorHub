<template>
  <div class="d-flex flex-column fill-height">
    <div><draft-action-bar /></div>
    <div><draft-info /></div>
    <div class="flex-grow-1 d-flex">
      <div><element-data-items category="before" /></div>
      <div class="flex-grow-1 d-flex flex-column">
        <div class="flex-grow-1">
          <element-editor />
        </div>
        <div><commit-files /></div>
      </div>
      <div><element-data-items category="after" /></div>
    </div>
    <element-type-colors />
  </div>
</template>

<script lang="ts">
import { defineComponent, onBeforeMount } from '@vue/composition-api'
import DraftActionBar from '@/components/draft/DraftActionBar.vue'
import DraftInfo from '@/components/draft/DraftInfo.vue'
import CommitFiles from '@/components/draft/CommitFiles/CommitFiles.vue'
import ElementDataItems from '@/components/draft/ElementDataItems/ElementDataItems.vue'
import ElementEditor from '@/components/draft/ElementEditor/ElementEditor.vue'
import ElementTypeColors from '@/components/draft/ElementTypeColors.vue'

export default defineComponent({
  name: 'draft',
  components: {
    DraftActionBar,
    DraftInfo,
    CommitFiles,
    ElementDataItems,
    ElementEditor,
    ElementTypeColors,
  },
  setup(_, { root }) {
    onBeforeMount(async () => {
      await root.$accessor.draft.initDraftStates(
        parseInt(root.$route.params.id)
      )
    })
  },
})
</script>
