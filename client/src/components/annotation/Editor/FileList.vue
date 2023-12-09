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
          <span class="mx-2 text-body-2 font-weight-medium path">{{
            fileTree.collapsedName
          }}</span>
        </div>
      </v-list-item>
    </template>
    <template
      v-for="fileName in fileTree.fileNames"
      :key="`${viewerId}:${fileName}`"
    >
      <v-list-item density="compact">
        <div class="d-flex align-center flex-nowrap" style="max-width: 100%">
          <v-icon
            v-if="getFilePair(fileName).status === 'modified'"
            size="small"
            icon="$mdiPencilBox"
            color="amber"
            style="min-width: max-content"
          />
          <v-icon
            v-if="getFilePair(fileName).status === 'added'"
            size="small"
            icon="$mdiPlusBox"
            color="green"
            style="min-width: max-content"
          />
          <v-icon
            v-if="getFilePair(fileName).status === 'removed'"
            size="small"
            icon="$mdiMinusBox"
            color="red"
            style="min-width: max-content"
          />
          <v-icon
            v-if="getFilePair(fileName).status === 'unmodified'"
            size="small"
            icon="$mdiEqualBox"
            color="brown"
            style="min-width: max-content"
          />
          <v-icon
            v-if="getFilePair(fileName).status === 'not found'"
            size="small"
            icon="$mdiCloseBox"
            color="grey"
            style="min-width: max-content"
          />
          <span class="text-body-2 font-weight-bold path mx-2">{{
            fileName
          }}</span>
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
                useViewer().recreateViewer(viewerId, {
                  type: 'file',
                  filePair,
                  category: 'before',
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
                useViewer().recreateViewer(viewerId, {
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
                useViewer().recreateViewer(viewerId, {
                  type: 'file',
                  filePair,
                  category: 'after',
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
          <v-icon
            v-if="getFilePair(fileName).status === 'renamed'"
            size="small"
            icon="$mdiArrowRightBoldBox"
            color="purple"
            style="min-width: max-content"
          />
          <span class="text-body-2 font-weight-bold ml-1 path">{{
            getFilePair(fileName).after?.path
          }}</span>
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
      ></file-list>
      <v-divider color="primary" />
    </template>
  </v-list-group>
</template>

<style lang="scss" scoped>
.path {
  display: flex;
  overflow-x: scroll;
  white-space: nowrap;
}
</style>
