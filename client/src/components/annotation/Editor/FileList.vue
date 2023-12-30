<script setup lang="ts">
import { FilePair } from 'composables/useAnnotation'
import { CollapsedDirectory } from 'utils/path'

const props = defineProps({
  viewerId: {
    type: String,
    required: true,
  },
  fileTree: {
    type: Object as () => CollapsedDirectory,
    required: true,
  },
  formerPath: {
    type: String,
    required: true,
  },
  onFileChange: {
    type: Function,
    required: true,
  },
})
function getFilePair(fileName: string): FilePair {
  const path =
    `${props.formerPath}/${props.fileTree.collapsedName}/${fileName}`.substring(
      '(Project Root)/'.length + 1,
    )
  const filePair = useAnnotation().getCurrentFilePair(path)
  if (!filePair)
    throw new Error(`cannot find filePair which path is ${path} in file list`)
  return filePair
}
</script>

<template>
  <v-list-group :value="`${viewerId}:${fileTree.collapsedName}`">
    <v-divider color="primary" />
    <template #activator="{ props: activatorProps }">
      <v-list-item variant="tonal" density="compact" v-bind="activatorProps">
        <div class="d-flex align-center flex-nowrap" style="max-width: 100%">
          <v-icon
            size="small"
            icon="$mdiFolderOutline"
            style="min-width: max-content"
          />
          <v-tooltip location="top center" origin="auto" :open-delay="500">
            <template #activator="{ props: tooltipProps }">
              <span
                v-bind="tooltipProps"
                class="mx-2 text-body-2 font-weight-medium text-shrink"
                >{{ fileTree.collapsedName }}</span
              >
            </template>
            {{ fileTree.collapsedName }}
          </v-tooltip>
        </div>
      </v-list-item>
    </template>
    <template
      v-for="fileName in fileTree.fileNames"
      :key="`${viewerId}:${fileName}`"
    >
      <v-list-item density="compact">
        <div class="d-flex align-center flex-nowrap" style="max-width: 100%">
          <v-tooltip location="top center" origin="auto" :open-delay="500">
            <template #activator="{ props: tooltipProps }">
              <v-icon
                v-if="getFilePair(fileName).status === 'modified'"
                v-bind="tooltipProps"
                size="small"
                icon="$mdiPencilBox"
                color="amber"
                style="min-width: max-content"
              />
            </template>
            This file is modified
          </v-tooltip>
          <v-tooltip location="top center" origin="auto" :open-delay="500">
            <template #activator="{ props: tooltipProps }">
              <v-icon
                v-if="getFilePair(fileName).status === 'added'"
                v-bind="tooltipProps"
                size="small"
                icon="$mdiPlusBox"
                color="green"
                style="min-width: max-content"
              />
            </template>
            This file is added
          </v-tooltip>
          <v-tooltip location="top center" origin="auto" :open-delay="500">
            <template #activator="{ props: tooltipProps }">
              <v-icon
                v-if="getFilePair(fileName).status === 'removed'"
                v-bind="tooltipProps"
                size="small"
                icon="$mdiMinusBox"
                color="red"
                style="min-width: max-content"
              />
            </template>
            This file is removed
          </v-tooltip>
          <v-tooltip location="top center" origin="auto" :open-delay="500">
            <template #activator="{ props: tooltipProps }">
              <v-icon
                v-if="getFilePair(fileName).status === 'unmodified'"
                v-bind="tooltipProps"
                size="small"
                icon="$mdiEqualBox"
                color="brown"
                style="min-width: max-content"
              />
            </template>
            This file has no changes; the source code is identical before and
            after the change
          </v-tooltip>
          <v-tooltip location="top center" origin="auto" :open-delay="500">
            <template #activator="{ props: tooltipProps }">
              <v-icon
                v-if="getFilePair(fileName).status === 'not found'"
                v-bind="tooltipProps"
                size="small"
                icon="$mdiCloseBox"
                color="grey"
                style="min-width: max-content"
              />
            </template>
            This file does not exist in the two snapshots before and after the
            change
          </v-tooltip>
          <v-tooltip location="top center" origin="auto" :open-delay="500">
            <template #activator="{ props: tooltipProps }">
              <span
                v-bind="tooltipProps"
                class="text-body-2 font-weight-bold text-shrink mx-2"
                >{{ fileName }}</span
              >
            </template>
            {{ fileName }}
          </v-tooltip>
          <v-spacer />
          <v-btn
            :color="colors.before"
            flat
            size="x-small"
            text="before"
            @click="
              (e: PointerEvent) => {
                e.stopPropagation() // prevent @click of v-sheet in MainViewer
                const filePair = getFilePair(fileName)
                useViewer().updateViewer(viewerId, {
                  type: 'file',
                  filePair,
                  navigation: {
                    category: 'before',
                  },
                })
                onFileChange()
              }
            "
          />
          <v-btn
            color="secondary"
            flat
            size="x-small"
            text="diff"
            @click="
              (e: PointerEvent) => {
                e.stopPropagation() // prevent @click of v-sheet in MainViewer
                const filePair = getFilePair(fileName)
                useViewer().updateViewer(viewerId, {
                  type: 'diff',
                  filePair,
                })
                onFileChange()
              }
            "
          />
          <v-btn
            :color="colors.after"
            flat
            size="x-small"
            text="after"
            @click="
              (e: PointerEvent) => {
                e.stopPropagation() // prevent @click of v-sheet in MainViewer
                const filePair = getFilePair(fileName)
                useViewer().updateViewer(viewerId, {
                  type: 'file',
                  filePair,
                  navigation: {
                    category: 'after',
                  },
                })
                onFileChange()
              }
            "
          />
        </div>
        <div
          v-if="getFilePair(fileName).status === 'renamed'"
          class="d-flex align-center flex-nowrap"
          style="max-width: 100%"
        >
          <v-tooltip location="top center" origin="auto" :open-delay="500">
            <template #activator="{ props: tooltipProps }">
              <v-icon
                v-if="getFilePair(fileName).status === 'renamed'"
                v-bind="tooltipProps"
                size="small"
                icon="$mdiArrowRightBoldBox"
                color="purple"
                style="min-width: max-content"
              />
            </template>
            This file is renamed
          </v-tooltip>
          <v-tooltip location="top center" origin="auto" :open-delay="500">
            <template #activator="{ props: tooltipProps }">
              <span
                v-bind="tooltipProps"
                class="text-body-2 font-weight-bold ml-1 text-shrink"
                >{{ getFilePair(fileName).after?.path }}</span
              >
            </template>
            {{ getFilePair(fileName).after?.path }}
          </v-tooltip>
        </div>
      </v-list-item>
      <v-divider />
    </template>
    <template
      v-for="directory in fileTree.directories"
      :key="`${viewerId}:${directory.collapsedName}`"
    >
      <file-list
        :viewer-id="viewerId"
        :file-tree="directory"
        :former-path="`${formerPath}/${fileTree.collapsedName}`"
        :on-file-change="onFileChange"
      />
      <v-divider color="primary" />
    </template>
  </v-list-group>
</template>

<style lang="scss" scoped>
.text-shrink {
  text-overflow: ellipsis;
  overflow-x: hidden;
  white-space: nowrap;
}
</style>
