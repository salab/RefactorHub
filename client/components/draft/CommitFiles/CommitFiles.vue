<template>
  <v-card flat tile>
    <v-expansion-panels focusable flat tile :value="0">
      <v-expansion-panel class="files-panel">
        <v-expansion-panel-header class="pa-0 pr-2">
          <commit-files-header />
        </v-expansion-panel-header>
        <v-expansion-panel-content>
          <v-divider />
          <div class="files-content d-flex">
            <div class="flex-grow-1">
              <commit-files-contents category="before" />
            </div>
            <div>
              <commit-files-icons />
            </div>
            <div class="flex-grow-1">
              <commit-files-contents category="after" />
            </div>
          </div>
        </v-expansion-panel-content>
      </v-expansion-panel>
    </v-expansion-panels>
  </v-card>
</template>

<script lang="ts">
import { defineComponent } from '@vue/composition-api'
import CommitFilesHeader from './CommitFilesHeader.vue'
import CommitFilesContents from './CommitFilesContents.vue'
import CommitFilesIcons from './CommitFilesIcons.vue'

export default defineComponent({
  name: 'CommitFiles',
  components: {
    CommitFilesHeader,
    CommitFilesContents,
    CommitFilesIcons,
  },
})
</script>

<style lang="scss" scoped>
.files-panel {
  &::v-deep {
    .v-expansion-panel-content__wrap {
      padding: 0;
    }
    .v-expansion-panel-header {
      min-height: 2.4rem;
      .v-expansion-panel-header__icon {
        .v-icon {
          font-size: 1.2rem;
        }
      }
    }
  }
}

.files-content {
  max-height: 12rem;
  overflow: scroll;
}
</style>
