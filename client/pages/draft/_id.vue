<template>
  <div class="d-flex flex-column fill-height">
    <div><commit-files /></div>
  </div>
</template>

<script lang="ts">
import { defineComponent, onBeforeMount } from '@vue/composition-api'
import CommitFiles from '@/components/draft/CommitFiles/CommitFiles.vue'

export default defineComponent({
  name: 'draft',
  components: {
    CommitFiles,
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
