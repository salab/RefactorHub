<template>
  <v-expansion-panels focusable tile :value="0">
    <v-expansion-panel>
      <v-expansion-panel-header class="files-panel-header d-flex pa-0 pr-2">
        <div class="pl-3 mr-6">
          <span
            v-if="commitFiles && selected.before !== undefined"
            :title="commitFiles[selected.before].previousName"
            class="subtitle-1"
            >{{ trimFileName(commitFiles[selected.before].previousName) }}</span
          >
        </div>
        <v-divider vertical />
        <div class="pl-3">
          <span
            v-if="commitFiles && selected.after !== undefined"
            :title="commitFiles[selected.after].name"
            class="subtitle-1"
            >{{ trimFileName(commitFiles[selected.after].name) }}</span
          >
        </div>
      </v-expansion-panel-header>
      <v-expansion-panel-content>
        <div class="file-list d-flex">
          <div class="flex-grow-1">
            <v-list v-if="commitFiles" dense>
              <v-list-item-group v-model="selected.before">
                <v-divider />
                <template v-for="(file, i) in commitFiles">
                  <v-list-item
                    :key="i"
                    :disabled="file.status === 'added' || i === selected.before"
                  >
                    <v-list-item-content>
                      <v-list-item-title
                        v-if="file.status !== 'added'"
                        :title="file.previousName"
                      >
                        {{ trimFileName(file.previousName, 70) }}
                      </v-list-item-title>
                    </v-list-item-content>
                  </v-list-item>
                  <v-divider :key="`before-divider-${i}`" />
                </template>
              </v-list-item-group>
            </v-list>
          </div>
          <div>
            <v-list v-if="commitFiles" dense>
              <v-list-item-group v-model="selected.common">
                <v-divider />
                <template v-for="(file, i) in commitFiles">
                  <v-list-item :key="i">
                    <v-icon
                      v-if="file.status === 'modified'"
                      small
                      color="amber"
                    >
                      fa-pen-square
                    </v-icon>
                    <v-icon v-if="file.status === 'added'" small color="green">
                      fa-plus-square
                    </v-icon>
                    <v-icon v-if="file.status === 'removed'" small color="red">
                      fa-minus-square
                    </v-icon>
                    <v-icon
                      v-if="file.status === 'renamed'"
                      small
                      color="purple"
                    >
                      fa-caret-square-right
                    </v-icon>
                  </v-list-item>
                  <v-divider :key="`common-divider-${i}`" />
                </template>
              </v-list-item-group>
            </v-list>
          </div>
          <div class="flex-grow-1">
            <v-list v-if="commitFiles" dense>
              <v-list-item-group v-model="selected.after">
                <v-divider />
                <template v-for="(file, i) in commitFiles">
                  <v-list-item
                    :key="i"
                    :selectable="false"
                    :disabled="file.status === 'removed' || i === after"
                  >
                    <v-list-item-content>
                      <v-list-item-title
                        v-if="file.status !== 'removed'"
                        :title="file.name"
                      >
                        {{ triming(file.name, 70) }}
                      </v-list-item-title>
                    </v-list-item-content>
                  </v-list-item>
                  <v-divider :key="`after-divider-${i}`" />
                </template>
              </v-list-item-group>
            </v-list>
          </div>
        </div>
      </v-expansion-panel-content>
    </v-expansion-panel>
  </v-expansion-panels>
</template>

<script lang="ts">
import { defineComponent, computed, reactive } from '@vue/composition-api'
import { DiffCategory, FileMetadata } from 'refactorhub'
import { trimFileName } from '@/components/common/editor/use/trim'

export default defineComponent({
  name: 'CommitFiles',
  setup(_, { root }) {
    const selected = reactive<{
      before?: number
      after?: number
      common?: number
    }>({
      before: 0,
      after: 0,
      common: 0,
    })

    const commitFiles = computed(() => root.$accessor.draft.commitInfo?.files)
    const displayedFileMetadata = computed(
      () => root.$accessor.draft.displayedFileMetadata
    )

    const changeDisplayedFileMetadata = (
      category: DiffCategory,
      metadata: FileMetadata
    ) => {
      root.$accessor.draft.setDisplayedFileMetadata({
        category,
        metadata,
      })
    }

    return {
      selected,
      commitFiles,
      displayedFileMetadata,
      changeDisplayedFileMetadata,
      trimFileName,
    }
  },
})
</script>

<style lang="scss" scope>
.files-panel-header {
  min-height: 36px !important;
}
.file-list {
  max-height: 200px;
  overflow: scroll;
}
</style>
