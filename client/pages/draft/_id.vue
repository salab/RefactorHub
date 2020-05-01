<template>
  <div class="d-flex flex-column fill-height">
    <div><commit-files /></div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from '@vue/composition-api'
import CommitFiles from '@/components/draft/CommitFiles/CommitFiles.vue'

export default defineComponent({
  name: 'draft',
  components: {
    CommitFiles,
  },
  setup() {
    return {}
  },
})
</script>
