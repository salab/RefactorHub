<template>
  <div class="d-flex flex-column fill-height">
    <div><draft-action-bar /></div>
    <div><draft-info /></div>
    <div><commit-files /></div>
    <element-type-colors />
  </div>
</template>

<script lang="ts">
import { defineComponent, onBeforeMount } from '@vue/composition-api'
import DraftActionBar from '@/components/draft/DraftActionBar.vue'
import DraftInfo from '@/components/draft/DraftInfo.vue'
import CommitFiles from '@/components/draft/CommitFiles/CommitFiles.vue'
import ElementTypeColors from '@/components/draft/ElementTypeColors.vue'

export default defineComponent({
  name: 'draft',
  components: {
    DraftActionBar,
    DraftInfo,
    CommitFiles,
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
