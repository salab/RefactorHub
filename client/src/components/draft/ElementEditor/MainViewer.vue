<script setup lang="ts">
import * as monaco from 'monaco-editor'
import { DiffCategory, ElementMetadata } from 'refactorhub'
import { CommonTokenSequenceDecorationManager } from './ts/commonTokensDecorations'
import { CodeFragmentsManager } from './ts/codeFragments'
import { ElementDecorationsManager } from './ts/elementDecorations'
import { ElementWidgetsManager } from './ts/elementWidgets'
import { logger } from '@/utils/logger'
import { DiffViewer, FileViewer, Viewer } from '@/composables/useViewer'
import { CommitDetail, CommitFile, RefactoringDraft } from '@/apis'

const props = defineProps({
  viewer: {
    type: Object as () => Viewer,
    required: true,
  },
})
const pending = ref(0)
const isLoading = computed(() => pending.value > 0)

const isOpeningFileList = ref(false)

const { mainViewerId, getNavigator } = useViewer()
const navigator = getNavigator(props.viewer.id)

const elementDecorationsManager = new ElementDecorationsManager()
const elementWidgetsManager = new ElementWidgetsManager()
const codeFragmentsManager = new CodeFragmentsManager()
const commonTokenSequenceDecorationManager =
  new CommonTokenSequenceDecorationManager()

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

function setupElementDecorations(
  fileViewer: monaco.editor.IStandaloneCodeEditor,
  category: DiffCategory,
  draft: RefactoringDraft,
  file: CommitFile,
) {
  if (!isExistingFile(category, file)) return
  elementDecorationsManager.clearElementDecorations(category)
  const path = getCommitFileName(category, file)
  Object.entries(draft.data[category]).forEach(([key, data]) => {
    data.elements.forEach((element, index) => {
      if (path === element.location?.path) {
        elementDecorationsManager.setElementDecorationOnEditor(
          category,
          key,
          index,
          element,
          fileViewer,
        )
      }
    })
  })
}
async function setupElementWidgets(
  fileViewer: monaco.editor.IStandaloneCodeEditor,
  category: DiffCategory,
  commit: CommitDetail,
  file: CommitFile,
) {
  if (!isExistingFile(category, file)) return
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

  elementWidgetsManager.clearElementWidgetsOnEditor(category, fileViewer)
  codeFragmentsManager.clearCodeFragmentCursors(category)
  content.elements.forEach((element) => {
    if (element.type === 'CodeFragment') {
      codeFragmentsManager.prepareCodeFragmentCursor(
        category,
        element,
        fileViewer,
      )
    } else {
      elementWidgetsManager.setElementWidgetOnEditor(
        category,
        element,
        fileViewer,
      )
    }
  })
}

function setupEditingElement(
  category: DiffCategory,
  metadata?: ElementMetadata,
) {
  if (metadata !== undefined) {
    if (metadata.type === 'CodeFragment') {
      codeFragmentsManager.setupCodeFragmentCursor(category)
      elementWidgetsManager.hideElementWidgets(category)
    } else {
      elementWidgetsManager.showElementWidgetsWithType(category, metadata.type)
      codeFragmentsManager.disposeCodeFragmentCursor(category)
    }
  } else {
    elementWidgetsManager.hideElementWidgets(category)
    codeFragmentsManager.disposeCodeFragmentCursor(category)
  }
}

let navigationDecoration: monaco.editor.IEditorDecorationsCollection | undefined

async function createDiffViewer(
  container: HTMLElement,
  viewer: DiffViewer,
  commit: CommitDetail,
  file: CommitFile,
): Promise<{
  diffViewer: monaco.editor.IStandaloneDiffEditor
  originalViewer: monaco.editor.IStandaloneCodeEditor
  modifiedViewer: monaco.editor.IStandaloneCodeEditor
}> {
  const { navigation } = viewer
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

  const originalViewer = diffViewer.getOriginalEditor()
  const modifiedViewer = diffViewer.getModifiedEditor()
  if (navigation) {
    const { category, range } = navigation
    const fileViewer = category === 'before' ? originalViewer : modifiedViewer
    navigationDecoration = fileViewer.createDecorationsCollection([
      {
        range,
        options: {
          className: `navigation-decoration`,
        },
      },
    ])
    setTimeout(() => fileViewer.revealRangeNearTop(range), 100)
  }

  return {
    diffViewer,
    originalViewer,
    modifiedViewer,
  }
}
async function createFileViewer(
  container: HTMLElement,
  viewer: FileViewer,
  commit: CommitDetail,
  file: CommitFile,
): Promise<{
  diffViewer?: undefined
  originalViewer?: monaco.editor.IStandaloneCodeEditor
  modifiedViewer?: monaco.editor.IStandaloneCodeEditor
}> {
  const { category, range } = viewer
  const fileViewer = monaco.editor.create(container, {
    automaticLayout: true,
    readOnly: true,
    scrollBeyondLastLine: false,
  })
  const textModel = await getTextModelOfFile(category, commit, file)
  fileViewer.setModel(textModel)

  const changedRanges: monaco.Range[] = []
  for (const { before, after } of file.diffHunks) {
    if (category === 'before' && before) {
      changedRanges.push(
        new monaco.Range(before.startLine, 1, before.endLine, 1),
      )
    }
    if (category === 'after' && after) {
      changedRanges.push(new monaco.Range(after.startLine, 1, after.endLine, 1))
    }
  }
  fileViewer.createDecorationsCollection(
    changedRanges.map((range) => ({
      range,
      options: {
        isWholeLine: true,
        className: `file-changed-${category}`,
      },
    })),
  )

  if (range) {
    navigationDecoration = fileViewer.createDecorationsCollection([
      {
        range,
        options: {
          className: `navigation-decoration`,
        },
      },
    ])
    setTimeout(() => fileViewer.revealRangeNearTop(range), 100)
  }

  if (category === 'before')
    return {
      originalViewer: fileViewer,
    }
  return {
    modifiedViewer: fileViewer,
  }
}

let commitFile: CommitFile | undefined
let diffViewer: monaco.editor.IStandaloneDiffEditor | undefined
let originalViewer: monaco.editor.IStandaloneCodeEditor | undefined
let modifiedViewer: monaco.editor.IStandaloneCodeEditor | undefined

async function createViewer(viewer: Viewer) {
  const container = document.getElementById(viewer.id)
  if (!container) {
    logger.error(`Cannot find the container element: id is ${viewer.id}`)
    return
  }
  const draft = useDraft().draft.value
  if (!draft) {
    logger.error('draft is not loaded')
    return
  }
  const commit = useDraft().commit.value
  if (!commit) {
    logger.error('commit is not loaded')
    return
  }
  const file =
    viewer.type === 'file'
      ? commit.files.find((file) =>
          viewer.category === 'before'
            ? file.previousName === viewer.path
            : file.name === viewer.path,
        )
      : commit.files.find(
          (file) =>
            file.previousName === viewer.beforePath ||
            file.name === viewer.afterPath,
        )
  if (!file) {
    logger.error(`commit file is not found: viewer=${viewer}`)
    return
  }
  commitFile = file

  const viewers =
    props.viewer.type === 'diff'
      ? await createDiffViewer(container, props.viewer, commit, file)
      : await createFileViewer(container, props.viewer, commit, file)
  diffViewer = viewers.diffViewer
  originalViewer = viewers.originalViewer
  modifiedViewer = viewers.modifiedViewer

  if (originalViewer) {
    setupElementDecorations(originalViewer, 'before', draft, file)
    await setupElementWidgets(originalViewer, 'before', commit, file)
    commonTokenSequenceDecorationManager.setCommonTokensDecorations(
      file.previousName,
      'before',
      originalViewer,
    )
  }
  setupEditingElement('before', useDraft().editingElement.value.before)
  if (modifiedViewer) {
    setupElementDecorations(modifiedViewer, 'after', draft, file)
    await setupElementWidgets(modifiedViewer, 'after', commit, file)
    commonTokenSequenceDecorationManager.setCommonTokensDecorations(
      file.name,
      'after',
      modifiedViewer,
    )
  }
  setupEditingElement('after', useDraft().editingElement.value.after)

  function onMouseDown(
    e: monaco.editor.IEditorMouseEvent,
    category: DiffCategory,
    file: CommitFile,
  ) {
    if (e.target.type !== monaco.editor.MouseTargetType.CONTENT_WIDGET) return
    const content = e.target.element?.textContent ?? ''
    if (!content.startsWith('View Common Token Sequence')) return

    const firstIndex = content.indexOf('(id=') + '(id='.length
    const lastIndex = content.indexOf(')')
    const commonTokenSequenceId = Number.parseInt(
      content.substring(firstIndex, lastIndex),
    )
    const { joinedRaw, tokenSequenceSet } = useCommonTokenSequence().getWithId(
      commonTokenSequenceId,
    )
    const sequencesOnThisViewer = tokenSequenceSet.filterCategory(category)
    const currentDestinationIndex = sequencesOnThisViewer.findIndex(
      (sequence) =>
        sequence.isIn(
          category === 'before' ? file.previousName : file.name,
          category,
        ),
    )
    useViewer().setNavigator(
      {
        label: joinedRaw,
        currentDestinationIndex:
          currentDestinationIndex === -1 ? 0 : currentDestinationIndex,
        destinations: sequencesOnThisViewer.map((sequence) => ({
          category,
          path: sequence.path,
          range: sequence.range,
        })),
      },
      props.viewer.id,
    )
    if (navigationDecoration) navigationDecoration.clear()

    const otherCategory = category === 'before' ? 'after' : 'before'
    const sequencesOnOtherViewer =
      tokenSequenceSet.filterCategory(otherCategory)
    const { id: newViewerId } = useViewer().createViewer(
      {
        type: 'file',
        category: otherCategory,
        path: sequencesOnOtherViewer[0].path,
        range: sequencesOnOtherViewer[0].range,
      },
      category === 'before' ? 'next' : 'prev',
    )
    useViewer().setNavigator(
      {
        label: joinedRaw,
        currentDestinationIndex: 0,
        destinations: sequencesOnOtherViewer.map((sequence) => ({
          category: otherCategory,
          path: sequence.path,
          range: sequence.range,
        })),
      },
      newViewerId,
    )
  }
  originalViewer?.onMouseDown((e) => onMouseDown(e, 'before', file))
  modifiedViewer?.onMouseDown((e) => onMouseDown(e, 'after', file))

  function onMouseMove(
    e: monaco.editor.IEditorMouseEvent,
    category: DiffCategory,
    file: CommitFile,
  ) {
    if (e.target.type !== monaco.editor.MouseTargetType.CONTENT_TEXT) return
    useCommonTokenSequence().updateIsHovered(
      category === 'before' ? file.previousName : file.name,
      category,
      e.target.position,
    )
  }
  originalViewer?.onMouseMove((e) => onMouseMove(e, 'before', file))
  modifiedViewer?.onMouseMove((e) => onMouseMove(e, 'after', file))
}

onMounted(async () => {
  pending.value++
  await createViewer(props.viewer)
  pending.value--
})

watch(
  () => useDraft().editingElement.value.before,
  (elementMetadata) => setupEditingElement('before', elementMetadata),
)
watch(
  () => useDraft().editingElement.value.after,
  (elementMetadata) => setupEditingElement('after', elementMetadata),
)
watch(
  () => useDraft().draft.value,
  () => {
    const draft = useDraft().draft.value
    if (!draft) {
      logger.error('draft is not loaded')
      return
    }
    if (commitFile && originalViewer) {
      setupElementDecorations(originalViewer, 'before', draft, commitFile)
    }
    if (commitFile && modifiedViewer) {
      setupElementDecorations(modifiedViewer, 'after', draft, commitFile)
    }
  },
)
watch(
  () => useCommonTokenSequence().setting.value,
  () => {
    if (commitFile && originalViewer) {
      commonTokenSequenceDecorationManager.clearCommonTokensDecorations(
        'before',
      )
      commonTokenSequenceDecorationManager.setCommonTokensDecorations(
        commitFile.previousName,
        'before',
        originalViewer,
      )
    }
    if (commitFile && modifiedViewer) {
      commonTokenSequenceDecorationManager.clearCommonTokensDecorations('after')
      commonTokenSequenceDecorationManager.setCommonTokensDecorations(
        commitFile.name,
        'after',
        modifiedViewer,
      )
    }
  },
)
</script>

<template>
  <v-sheet
    border
    :color="mainViewerId === viewer.id ? 'primary' : 'background'"
    class="d-flex flex-column fill-height"
    @click="() => (useViewer().mainViewerId.value = viewer.id)"
  >
    <div class="d-flex align-center flex-nowrap" style="max-width: 100%">
      <v-menu transition="slide-y-transition">
        <template #activator="{ props: menuProps }">
          <v-btn
            v-if="viewer.type === 'file' && viewer.category === 'before'"
            :color="colors.before"
            flat
            size="x-small"
            text="before"
            class="mx-1"
            v-bind="menuProps"
          />
          <v-btn
            v-if="viewer.type === 'diff'"
            color="secondary"
            flat
            size="x-small"
            text="diff"
            class="mx-1"
            v-bind="menuProps"
          />
          <v-btn
            v-if="viewer.type === 'file' && viewer.category === 'after'"
            :color="colors.after"
            flat
            size="x-small"
            text="after"
            class="mx-1"
            v-bind="menuProps"
          />
        </template>
        <v-btn-group variant="elevated" :elevation="5" density="compact">
          <v-btn
            v-if="commitFile?.status !== 'added'"
            :color="colors.before"
            size="small"
            text="before"
            @click="
              (e: PointerEvent) => {
                if (!commitFile) return
                e.stopPropagation() // prevent @click of v-sheet in MainViewer
                useViewer().recreateViewer(viewer.id, {
                  type: 'file',
                  category: 'before',
                  path: commitFile.previousName,
                })
              }
            "
          />
          <v-btn
            v-if="
              commitFile?.status !== 'added' && commitFile?.status !== 'removed'
            "
            color="secondary"
            size="small"
            text="diff"
            @click="
              (e: PointerEvent) => {
                if (!commitFile) return
                e.stopPropagation() // prevent @click of v-sheet in MainViewer
                useViewer().recreateViewer(viewer.id, {
                  type: 'diff',
                  beforePath: commitFile.previousName,
                  afterPath: commitFile.name,
                })
              }
            "
          />
          <v-btn
            v-if="commitFile?.status !== 'removed'"
            :color="colors.after"
            size="small"
            text="after"
            @click="
              (e: PointerEvent) => {
                if (!commitFile) return
                e.stopPropagation() // prevent @click of v-sheet in MainViewer
                useViewer().recreateViewer(viewer.id, {
                  type: 'file',
                  category: 'after',
                  path: commitFile.name,
                })
              }
            "
          />
        </v-btn-group>
      </v-menu>
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
        <v-icon
          size="small"
          icon="$mdiArrowRightBoldBox"
          color="purple"
          style="min-width: max-content"
        />
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
      <v-divider v-if="navigator" vertical />
      <v-btn
        v-if="navigator"
        variant="plain"
        density="compact"
        :icon="'$mdiMenuLeftOutline'"
        flat
        :title="`show previous`"
        @click="
          (e: PointerEvent) => {
            e.stopPropagation() // prevent @click of v-sheet in MainViewer
            useViewer().navigate(viewer.id, 'prev')
          }
        "
      />
      <code
        v-if="navigator"
        class="path"
        style="
          max-width: 20%;
          border: 0.5px solid black;
          background-color: rgba(255, 250, 240, 0.7);
        "
        >{{ navigator.label }}</code
      >
      <span v-if="navigator" class="text-body-2 ml-1">{{
        `${navigator.currentDestinationIndex + 1}/${
          navigator.destinations.length
        }`
      }}</span>
      <v-btn
        v-if="navigator"
        variant="plain"
        density="compact"
        :icon="'$mdiMenuRightOutline'"
        flat
        :title="`show next`"
        @click="
          (e: PointerEvent) => {
            e.stopPropagation() // prevent @click of v-sheet in MainViewer
            useViewer().navigate(viewer.id, 'next')
          }
        "
      />
      <v-btn
        v-if="navigator"
        variant="plain"
        density="compact"
        :icon="'$mdiCloseCircleOutline'"
        flat
        :title="`delete navigation`"
        class="mr-1"
        @click="
          () => {
            useViewer().deleteNavigator(viewer.id)
            navigationDecoration?.clear()
          }
        "
      />
      <v-divider v-if="navigator" vertical />
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
          <h2 class="mx-1">Changed File List</h2>
          <v-list :opened="[`${viewer.id}:(Project Root)`]" class="py-0 mx-1"
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
  .navigation-decoration {
    border: 0.5px solid black;
    background-color: rgba(255, 250, 240, 0.7);
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
}
</style>
<style lang="scss">
*,
::before,
::after {
  background-repeat: repeat !important; // in order to display shaded area in diff editor
}
</style>
