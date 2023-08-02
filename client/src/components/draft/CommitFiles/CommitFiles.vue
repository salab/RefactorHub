<script setup lang="ts">
import { CommitFile } from '@/apis'

defineProps({
  files: {
    type: Array as () => CommitFile[],
    required: true,
  },
})
const { displayedFile } = useDraft()
onMounted(() => {
  displayedFile.value = {
    before: { index: 0 },
    after: { index: 0 },
  }
})
</script>

<template>
  <v-card flat>
    <v-expansion-panels>
      <v-expansion-panel class="files-panel">
        <v-expansion-panel-title class="pa-0 pr-2">
          <commit-files-header :files="files" />
        </v-expansion-panel-title>
        <v-expansion-panel-text>
          <v-divider />
          <div class="files-content d-flex">
            <div class="flex-grow-1">
              <commit-files-contents :files="files" category="before" />
            </div>
            <div>
              <commit-files-icons :files="files" />
            </div>
            <div class="flex-grow-1">
              <commit-files-contents :files="files" category="after" />
            </div>
          </div>
        </v-expansion-panel-text>
      </v-expansion-panel>
    </v-expansion-panels>
  </v-card>
</template>

<style lang="scss" scoped>
.files-panel {
  ::v-deep(&) {
    .v-expansion-panel-text__wrap {
      padding: 0;
    }
    .v-expansion-panel-title {
      min-height: 2.4rem;
      .v-expansion-panel-title__icon {
        .v-icon {
          font-size: 1.2rem;
        }
      }
    }
  }
}

.files-content {
  max-height: 12rem;
  overflow-y: scroll;
}
</style>
