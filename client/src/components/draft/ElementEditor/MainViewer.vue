<script setup lang="ts">
import * as monaco from 'monaco-editor'
import { DiffCategory } from 'refactorhub'
import { getCommonTokensSetOf } from './ts/commonTokensDecorations'
import { logger } from '@/utils/logger'
import { DiffViewer, FileViewer, Viewer } from '@/composables/useViewer'
import { CommitDetail, CommitFile } from '@/apis'

const props = defineProps({
  viewer: {
    type: Object as () => Viewer,
    required: true,
  },
})
const pending = ref(0)
const isLoading = computed(() => pending.value > 0)

const isOpeningFileList = ref(false)

const { mainViewerId } = useViewer()

function isExistingFile(category: DiffCategory, file: CommitFile) {
  return !(
    (category === 'before' && file.status === 'added') ||
    (category === 'after' && file.status === 'removed')
  )
}
function getCommitSHA(category: DiffCategory, commit: CommitDetail) {
  return category === 'before' ? commit.parent : commit.sha
}
function getCommitFileName(category: DiffCategory, file: CommitFile) {
  return category === 'before' ? file.previousName : file.name
}
function getCommitFileUri(
  owner: string,
  repository: string,
  sha: string,
  path: string,
) {
  return `https://github.com/${owner}/${repository}/blob/${sha}/${path}`
}
async function getTextModelOfFile(
  category: DiffCategory,
  commit: CommitDetail,
  file: CommitFile,
) {
  if (!isExistingFile(category, file))
    return monaco.editor.createModel('', 'text/plain')

  const sha = getCommitSHA(category, commit)
  const path = getCommitFileName(category, file)
  const content = await useDraft().getFileContent({
    owner: commit.owner,
    repository: commit.repository,
    sha: commit.sha,
    category,
    path,
    uri: getCommitFileUri(commit.owner, commit.repository, sha, path),
  })
  return (
    monaco.editor.getModel(monaco.Uri.parse(content.uri)) ||
    monaco.editor.createModel(
      content.text,
      content.extension === 'java' ? 'java' : 'text/plain',
      monaco.Uri.parse(content.uri),
    )
  )
}

async function createDiffViewer(
  container: HTMLElement,
  viewer: DiffViewer,
): Promise<{
  diffViewer: monaco.editor.IStandaloneDiffEditor
  originalViewer: monaco.editor.IStandaloneCodeEditor
  modifiedViewer: monaco.editor.IStandaloneCodeEditor
}> {
  const commit = useDraft().commit.value
  if (!commit) throw new Error('commit is not loaded')
  const file = commit.files.find(
    (file) => file.previousName === viewer.beforePath,
  )
  if (!file)
    throw new Error(`commit file is not found: beforePath=${viewer.beforePath}`)

  const changes: monaco.editor.LineRangeMapping[] = []
  let beforeLineNumber = 0
  let afterLineNumber = 0
  for (const { before, after } of file.diffHunks) {
    if (before) {
      if (!after) afterLineNumber += before.startLine - beforeLineNumber
      beforeLineNumber = before.startLine
    }
    if (after) {
      if (!before) beforeLineNumber += after.startLine - afterLineNumber
      afterLineNumber = after.startLine
    }
    const originalRange = before
      ? new monaco.editor.LineRange(beforeLineNumber, before.endLine + 1)
      : new monaco.editor.LineRange(beforeLineNumber, beforeLineNumber)
    const modifiedRange = after
      ? new monaco.editor.LineRange(afterLineNumber, after.endLine + 1)
      : new monaco.editor.LineRange(afterLineNumber, afterLineNumber)
    if (before) beforeLineNumber = before.endLine + 1
    if (after) afterLineNumber = after.endLine + 1
    changes.push(
      new monaco.editor.LineRangeMapping(
        originalRange,
        modifiedRange,
        undefined,
      ),
    )
  }

  const diffViewer = monaco.editor.createDiffEditor(container, {
    enableSplitViewResizing: true,
    automaticLayout: true,
    readOnly: true,
    scrollBeyondLastLine: false,
    diffAlgorithm: {
      onDidChange: () => ({ dispose: () => {} }),
      computeDiff: () =>
        new Promise((resolve) =>
          resolve({
            identical: file.diffHunks.length === 0,
            quitEarly: false,
            changes,
            moves: [],
          }),
        ),
    },
  })

  const originalTextModel = await getTextModelOfFile('before', commit, file)
  const modifiedTextModel = await getTextModelOfFile('after', commit, file)
  diffViewer.setModel({
    original: originalTextModel,
    modified: modifiedTextModel,
  })

  return {
    diffViewer,
    originalViewer: diffViewer.getOriginalEditor(),
    modifiedViewer: diffViewer.getModifiedEditor(),
  }
}
async function createFileViewer(
  container: HTMLElement,
  viewer: FileViewer,
): Promise<{
  diffViewer?: undefined
  originalViewer?: monaco.editor.IStandaloneCodeEditor
  modifiedViewer?: monaco.editor.IStandaloneCodeEditor
}> {
  const commit = useDraft().commit.value
  if (!commit) throw new Error('commit is not loaded')
  const file = commit.files.find((file) =>
    viewer.category === 'before'
      ? file.previousName === viewer.path
      : file.name === viewer.path,
  )
  if (!file) throw new Error(`commit file is not found: path=${viewer.path}`)

  const fileViewer = monaco.editor.create(container, {
    automaticLayout: true,
    readOnly: true,
    scrollBeyondLastLine: false,
  })
  const textModel = await getTextModelOfFile(viewer.category, commit, file)
  fileViewer.setModel(textModel)

  const changedRanges: monaco.Range[] = []
  for (const { before, after } of file.diffHunks) {
    if (viewer.category === 'before' && before) {
      changedRanges.push(
        new monaco.Range(before.startLine, 1, before.endLine, 1),
      )
    }
    if (viewer.category === 'after' && after) {
      changedRanges.push(new monaco.Range(after.startLine, 1, after.endLine, 1))
    }
  }
  fileViewer.createDecorationsCollection(
    changedRanges.map((range) => ({
      range,
      options: {
        isWholeLine: true,
        className: `file-changed-${viewer.category}`,
      },
    })),
  )

  if (viewer.category === 'before')
    return {
      originalViewer: fileViewer,
    }
  return {
    modifiedViewer: fileViewer,
  }
}

async function createViewer() {
  const container = document.getElementById(props.viewer.id)
  if (!container) {
    logger.error(`Cannot find the container element: id is ${props.viewer.id}`)
    return
  }
  pending.value++
  const { diffViewer, originalViewer, modifiedViewer } =
    props.viewer.type === 'diff'
      ? await createDiffViewer(container, props.viewer)
      : await createFileViewer(container, props.viewer)

  function onMouseDown(e: monaco.editor.IEditorMouseEvent) {
    if (e.target.type === monaco.editor.MouseTargetType.CONTENT_WIDGET) {
      const content = e.target.element?.textContent ?? ''
      if (content.startsWith('View Common Tokens: ')) {
        const commonTokensRaw = content.substring('View Common Tokens: '.length)
        const commonTokensSet = getCommonTokensSetOf(commonTokensRaw)

        // selectedCommonTokens.raw = commonTokensRaw
        // selectedCommonTokens.count = commonTokensSet.size
        // selectedCommonTokens.before = [...commonTokensSet].filter(
        //   (c) => c.category === 'before',
        // )
        // selectedCommonTokens.after = [...commonTokensSet].filter(
        //   (c) => c.category === 'after',
        // )
        // commonTokensSelected.before = true
        // commonTokensSelected.after = true
      }
    }
  }
  originalViewer?.onMouseDown((e) => onMouseDown(e))
  modifiedViewer?.onMouseDown((e) => onMouseDown(e))

  function onMouseMove(
    e: monaco.editor.IEditorMouseEvent,
    diffCategory: DiffCategory,
  ) {
    if (e.target.type !== monaco.editor.MouseTargetType.CONTENT_TEXT) return
    // updateCommonTokensDecorationsOnBothDiffEditor(
    //   diffEditor,
    //   e.target.position ? [diffCategory, e.target.position] : undefined,
    // )
  }
  originalViewer?.onMouseMove((e) => onMouseMove(e, 'before'))
  modifiedViewer?.onMouseMove((e) => onMouseMove(e, 'after'))

  pending.value--
}

onMounted(async () => await createViewer())
</script>

<template>
  <v-sheet
    border
    :color="mainViewerId === viewer.id ? 'primary' : 'background'"
    class="d-flex flex-column fill-height"
    @click="() => (useViewer().mainViewerId.value = viewer.id)"
  >
    <div class="d-flex align-center flex-nowrap" style="max-width: 100%">
      <v-btn
        v-if="viewer.type === 'file' && viewer.category === 'before'"
        :color="colors.before"
        flat
        size="x-small"
        text="before"
        class="mx-1"
      />
      <v-btn
        v-if="viewer.type === 'diff'"
        color="secondary"
        flat
        size="x-small"
        text="diff"
        class="mx-1"
      />
      <v-btn
        v-if="viewer.type === 'file' && viewer.category === 'after'"
        :color="colors.after"
        flat
        size="x-small"
        text="after"
        class="mx-1"
      />
      <span
        v-if="viewer.type === 'file'"
        class="flex-shrink-1 mx-1 path text-subtitle-2"
        :style="`background-color: ${colors[viewer.category]}; min-width: 0%`"
      >
        {{ getFileName(viewer.path) }}
      </span>
      <div
        v-else
        class="flex-shrink-1 mx-1 d-flex align-center flex-nowrap"
        style="min-width: 0%"
      >
        <span
          class="path text-subtitle-2"
          :style="`background-color: ${colors.before}`"
        >
          {{ getPathDifference(viewer.beforePath, viewer.afterPath)[0] }}
        </span>
        <v-icon size="small" icon="$mdiArrowRightBoldBox" color="purple" />
        <span
          class="path text-subtitle-2"
          :style="`background-color: ${colors.after}`"
        >
          {{ getPathDifference(viewer.beforePath, viewer.afterPath)[1] }}
        </span>
      </div>
      <v-btn
        variant="plain"
        density="compact"
        :icon="
          isOpeningFileList
            ? '$mdiArrowUpDropCircleOutline'
            : '$mdiArrowDownDropCircleOutline'
        "
        flat
        :title="`${isOpeningFileList ? `close` : `open`} file list`"
        @click="() => (isOpeningFileList = !isOpeningFileList)"
      />
      <v-spacer />
      <v-btn
        v-if="useViewer().viewers.value.length > 1"
        variant="plain"
        density="compact"
        :icon="'$mdiTabRemove'"
        flat
        :title="`delete this window`"
        @click="
          (e: PointerEvent) => {
            e.stopPropagation() // prevent @click of v-sheet in MainViewer
            useViewer().deleteViewer(viewer.id)
          }
        "
      />
      <v-btn
        variant="plain"
        density="compact"
        :icon="'$mdiTabPlus'"
        flat
        :title="`duplicate this window`"
        @click="
          (e: PointerEvent) => {
            e.stopPropagation() // prevent @click of v-sheet in MainViewer
            useViewer().duplicateViewer(viewer.id)
          }
        "
      />
    </div>
    <v-divider />
    <div class="flex-grow-1 position-relative">
      <v-expand-transition style="position: absolute; z-index: 100">
        <v-sheet v-if="isOpeningFileList" style="width: 100%; height: 100%">
          <v-list :opened="[`${viewer.id}:(Project Root)`]"
            ><file-list
              :viewer-id="viewer.id"
              :file-tree="
                getFileTreeStructure(
                  useDraft().commit.value?.files.map((file) =>
                    file.status === 'added' ? file.name : file.previousName,
                  ) ?? [],
                  '(Project Root)',
                )
              "
              former-path=""
              :on-file-change="() => (isOpeningFileList = !isOpeningFileList)"
            />
          </v-list>
        </v-sheet>
      </v-expand-transition>
      <div :id="viewer.id" class="wh-100 element-editor">
        <loading-circle :active="isLoading" />
      </div>
    </div>
  </v-sheet>
</template>

<style lang="scss" scoped>
.position-relative {
  position: relative;
}
.wh-100 {
  width: 100%;
  height: 100%;
}

.path {
  display: flex;
  overflow-x: scroll;
  white-space: nowrap;
}

::v-deep(.element-editor) {
  .file-changed-before {
    background: rgba(255, 98, 88, 0.5);
  }
  .file-changed-after {
    background: rgba(175, 208, 107, 0.5);
  }

  .element-widget {
    cursor: pointer;
    border: 2px solid;
    opacity: 0.6;
    &:hover {
      opacity: 1;
    }
  }
  .element-decoration {
    border: 1px solid;
  }
  // .commonTokens-decoration {}
}
</style>
<style lang="scss">
*,
::before,
::after {
  background-repeat: repeat !important; // in order to display shaded area in diff editor
}
</style>
