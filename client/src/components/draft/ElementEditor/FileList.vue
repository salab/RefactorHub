<script setup lang="ts">
import { CommitFile } from 'apis'
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
function getFile(fileName: string): CommitFile {
  const path = (
    props.fileTree.collapsedName.substring(0, '(Project Root)'.length) ===
    '(Project Root)'
      ? `${props.fileTree.collapsedName.substring(
          '(Project Root)'.length,
        )}/${fileName}`
      : `${props.formerPath}/${props.fileTree.collapsedName}/${fileName}`
  ).substring(1)
  const file = useDraft().commit.value?.files.find(
    (file) => file.previousName === path || file.name === path,
  )
  if (!file)
    throw new Error(
      `cannot find file ${path} in file list ${useDraft()
        .commit.value?.files.map((file) => `${file.previousName}=>${file.name}`)
        .join('\n')}`,
    )
  return file
}
</script>

<template>
  <v-list-group :value="`${viewerId}:${fileTree.collapsedName}`">
    <v-divider color="primary" />
    <template #activator="{ props: activatorProps }">
      <v-list-item variant="tonal" density="compact" v-bind="activatorProps"
        ><span class="text-body-2 font-weight-medium path">{{
          fileTree.collapsedName
        }}</span></v-list-item
      >
    </template>
    <template
      v-for="fileName in fileTree.fileNames"
      :key="`${viewerId}:${fileName}`"
    >
      <v-list-item density="compact">
        <div class="d-flex align-center flex-nowrap" style="max-width: 100%">
          <v-icon
            v-if="getFile(fileName).status === 'modified'"
            size="small"
            icon="$mdiPencilBox"
            color="amber"
          />
          <v-icon
            v-if="getFile(fileName).status === 'added'"
            size="small"
            icon="$mdiPlusBox"
            color="green"
          />
          <v-icon
            v-if="getFile(fileName).status === 'removed'"
            size="small"
            icon="$mdiMinusBox"
            color="red"
          />
          <span class="text-body-2 font-weight-bold path mx-2">{{
            fileName
          }}</span>
          <v-spacer />
          <v-btn
            v-if="
              ['removed', 'modified', 'renamed'].includes(
                getFile(fileName).status,
              )
            "
            :color="colors.before"
            flat
            size="x-small"
            text="before"
            @click="
              () => {
                const file = getFile(fileName)
                useViewer().recreateViewer(viewerId, {
                  id: viewerId,
                  type: 'file',
                  category: 'before',
                  path: file.previousName,
                  navigator: {
                    label: '',
                    navigations: [],
                  },
                })
                onFileChange()
              }
            "
          />
          <v-btn
            v-if="['modified', 'renamed'].includes(getFile(fileName).status)"
            color="secondary"
            flat
            size="x-small"
            text="diff"
            @click="
              () => {
                const file = getFile(fileName)
                useViewer().recreateViewer(viewerId, {
                  id: viewerId,
                  type: 'diff',
                  beforePath: file.previousName,
                  afterPath: file.name,
                  navigator: {
                    label: '',
                    navigations: [],
                  },
                })
                onFileChange()
              }
            "
          />
          <v-btn
            v-if="
              ['added', 'modified', 'renamed'].includes(
                getFile(fileName).status,
              )
            "
            :color="colors.after"
            flat
            size="x-small"
            text="after"
            @click="
              () => {
                const file = getFile(fileName)
                useViewer().recreateViewer(viewerId, {
                  id: viewerId,
                  type: 'file',
                  category: 'after',
                  path: file.name,
                  navigator: {
                    label: '',
                    navigations: [],
                  },
                })
                onFileChange()
              }
            "
          />
        </div>
        <div
          v-if="getFile(fileName).status === 'renamed'"
          class="d-flex align-center flex-nowrap"
          style="max-width: 100%"
        >
          <v-icon
            v-if="getFile(fileName).status === 'renamed'"
            size="small"
            icon="$mdiArrowRightBoldBox"
            color="purple"
          />
          <span class="text-body-2 font-weight-bold ml-1 path">{{
            getFile(fileName).name
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
        :former-path="
          fileTree.collapsedName === '(Project Root)'
            ? ''
            : `${formerPath}/${fileTree.collapsedName}`
        "
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
