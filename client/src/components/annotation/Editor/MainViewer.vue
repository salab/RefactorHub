<script setup lang="ts">
import * as monaco from 'monaco-editor'
import { DiffCategory } from 'refactorhub'
import { CommonTokenSequenceDecorationManager } from './ts/commonTokensDecorations'
import { CodeFragmentManager } from './ts/codeFragments'
import { ElementDecorationManager } from './ts/elementDecorations'
import { ElementWidgetManager } from './ts/elementWidgets'
import { logger } from '@/utils/logger'
import { DiffViewer, FileViewer, Viewer } from '@/composables/useViewer'
import apis from '@/apis'

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

const canModifyAfter = computed(
  () =>
    useAnnotation().annotation.value?.ownerId ===
      (useUser().user.value?.id ?? '') &&
    !!useAnnotation().annotation.value?.hasTemporarySnapshot &&
    !props.viewer.filePair.isAlreadyRemoved() &&
    useAnnotation()
      .getChangeList()
      .findIndex(({ id }) => id === useAnnotation().currentChange.value?.id) ===
      useAnnotation().getChangeList().length - 2,
)
const canModifyBefore = computed(
  () =>
    useAnnotation().annotation.value?.ownerId ===
      (useUser().user.value?.id ?? '') &&
    !!useAnnotation().annotation.value?.hasTemporarySnapshot &&
    !props.viewer.filePair.isAlreadyRemoved() &&
    useAnnotation()
      .getChangeList()
      .findIndex(({ id }) => id === useAnnotation().currentChange.value?.id) ===
      useAnnotation().getChangeList().length - 1,
)
const canModify = computed(
  () =>
    (canModifyAfter.value &&
      props.viewer.type === 'file' &&
      props.viewer.category === 'after') ||
    (canModifyBefore.value &&
      props.viewer.type === 'file' &&
      props.viewer.category === 'before'),
)

function createDiffViewer(
  container: HTMLElement,
  viewer: DiffViewer,
): {
  diffViewer: monaco.editor.IStandaloneDiffEditor
  originalViewer: monaco.editor.IStandaloneCodeEditor
  modifiedViewer: monaco.editor.IStandaloneCodeEditor
} {
  const { filePair, navigation } = viewer
  const changes: monaco.editor.LineRangeMapping[] = []
  let beforeLineNumber = 0
  let afterLineNumber = 0
  for (const { before, after } of filePair.diffHunks) {
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
    renderWhitespace: 'all',
    diffAlgorithm: {
      onDidChange: () => ({ dispose: () => {} }),
      computeDiff: () =>
        new Promise((resolve) =>
          resolve({
            identical: filePair.diffHunks.length === 0,
            quitEarly: false,
            changes,
            moves: [],
          }),
        ),
    },
  })

  const originalTextModel = useAnnotation().getTextModel(filePair, 'before')
  const modifiedTextModel = useAnnotation().getTextModel(filePair, 'after')
  diffViewer.setModel({
    original: originalTextModel,
    modified: modifiedTextModel,
  })

  const originalViewer = diffViewer.getOriginalEditor()
  const modifiedViewer = diffViewer.getModifiedEditor()
  if (navigation) {
    const { category, range } = navigation
    const fileViewer = category === 'before' ? originalViewer : modifiedViewer
    setTimeout(() => fileViewer.revealRangeAtTop(range), 100)
  }

  return {
    diffViewer,
    originalViewer,
    modifiedViewer,
  }
}
function createFileViewer(
  container: HTMLElement,
  viewer: FileViewer,
): {
  diffViewer?: undefined
  originalViewer?: monaco.editor.IStandaloneCodeEditor
  modifiedViewer?: monaco.editor.IStandaloneCodeEditor
} {
  const { filePair, category, navigation } = viewer
  const fileViewer = monaco.editor.create(container, {
    automaticLayout: true,
    readOnly: !canModify.value,
    renderWhitespace: 'all',
  })
  const textModel = useAnnotation().getTextModel(filePair, category)
  fileViewer.setModel(textModel)

  const changedRanges: monaco.Range[] = []
  for (const { before, after } of filePair.diffHunks) {
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

  if (navigation) {
    setTimeout(() => fileViewer.revealRangeAtTop(navigation.range), 100)
  }

  if (category === 'before')
    return {
      originalViewer: fileViewer,
    }
  return {
    modifiedViewer: fileViewer,
  }
}

let latestMousePosition: monaco.Position | undefined
let originalViewer: monaco.editor.IStandaloneCodeEditor | undefined
let modifiedViewer: monaco.editor.IStandaloneCodeEditor | undefined

async function createViewer(viewer: Viewer) {
  const container = document.getElementById(viewer.id)
  if (!container) {
    logger.error(`Cannot find the container element: id is ${viewer.id}`)
    return
  }
  const { filePair } = viewer

  const viewers =
    props.viewer.type === 'diff'
      ? await createDiffViewer(container, props.viewer)
      : await createFileViewer(container, props.viewer)
  originalViewer = viewers.originalViewer
  modifiedViewer = viewers.modifiedViewer

  const elementDecorationManager = new ElementDecorationManager(
    filePair.getPathPair(),
    originalViewer,
    modifiedViewer,
  )
  const codeFragmentManager = new CodeFragmentManager(
    filePair.getPathPair(),
    filePair.before?.elements ?? [],
    filePair.after?.elements ?? [],
    originalViewer,
    modifiedViewer,
  )
  const elementWidgetManager = new ElementWidgetManager(
    filePair.before?.elements ?? [],
    filePair.after?.elements ?? [],
    originalViewer,
    modifiedViewer,
  )
  const commonTokenSequenceDecorationManager =
    new CommonTokenSequenceDecorationManager(
      filePair.getPathPair(),
      originalViewer,
      modifiedViewer,
    )

  watch(
    () => props.viewer.filePair,
    (newFilePair) => {
      elementDecorationManager.update(newFilePair.getPathPair())
      codeFragmentManager.update(
        newFilePair.getPathPair(),
        newFilePair.before?.elements ?? [],
        newFilePair.after?.elements ?? [],
      )
      elementWidgetManager.update(
        newFilePair.before?.elements ?? [],
        newFilePair.after?.elements ?? [],
      )
      commonTokenSequenceDecorationManager.update(newFilePair.getPathPair())
    },
  )

  function onMouseDown(
    e: monaco.editor.IEditorMouseEvent,
    category: DiffCategory,
  ) {
    if (e.target.type !== monaco.editor.MouseTargetType.CONTENT_WIDGET) return
    const commonTokenSequenceId =
      commonTokenSequenceDecorationManager.getIdFromHoverMessageClickEvent(e)
    if (commonTokenSequenceId === undefined) return
    const { joinedRaw, tokenSequenceSet } = useCommonTokenSequence().getWithId(
      commonTokenSequenceId,
    )
    useCommonTokenSequence().updateSelectedId(commonTokenSequenceId)
    const sequencesOnThisViewer = tokenSequenceSet.filterCategory(category)
    const currentPath =
      category === 'before' ? filePair.before?.path : filePair.after?.path
    if (currentPath === undefined)
      throw new Error('cannot get currentPath of viewer')
    const currentDestinationIndex = sequencesOnThisViewer.findIndex(
      (sequence) =>
        latestMousePosition &&
        sequence.isIn(currentPath, category) &&
        sequence.range.containsPosition(latestMousePosition),
    )
    useViewer().setNavigator(
      {
        label: joinedRaw,
        currentDestinationIndex:
          currentDestinationIndex === -1 ? 0 : currentDestinationIndex,
        destinations: sequencesOnThisViewer.map((sequence) => ({
          path: sequence.path,
          category,
          range: sequence.range,
        })),
      },
      props.viewer.id,
    )

    const otherCategory = category === 'before' ? 'after' : 'before'
    const sequencesOnOtherViewer =
      tokenSequenceSet.filterCategory(otherCategory)
    const filePairOnOtherViewer = useAnnotation().getCurrentFilePair(
      sequencesOnOtherViewer[0].path,
    )
    if (!filePairOnOtherViewer) {
      throw new Error(
        `cannot find filePair; path=${sequencesOnOtherViewer[0].path}`,
      )
    }
    const { id: newViewerId } = useViewer().createViewer(
      {
        type: 'file',
        filePair: filePairOnOtherViewer,
        category: otherCategory,
        navigation: {
          category: otherCategory,
          range: sequencesOnOtherViewer[0].range,
        },
      },
      category === 'before' ? 'next' : 'prev',
    )
    useViewer().setNavigator(
      {
        label: joinedRaw,
        currentDestinationIndex: 0,
        destinations: sequencesOnOtherViewer.map((sequence) => ({
          path: sequence.path,
          category: otherCategory,
          range: sequence.range,
        })),
      },
      newViewerId,
    )
  }
  originalViewer?.onMouseDown((e) => onMouseDown(e, 'before'))
  modifiedViewer?.onMouseDown((e) => onMouseDown(e, 'after'))

  function onMouseMove(
    e: monaco.editor.IEditorMouseEvent,
    category: DiffCategory,
  ) {
    if (e.target.position) latestMousePosition = e.target.position
    if (e.target.type !== monaco.editor.MouseTargetType.CONTENT_TEXT) return
    const path = filePair.getPathPair()[category]
    if (path === undefined) return
    useCommonTokenSequence().updateIsHovered(path, category, e.target.position)
  }
  originalViewer?.onMouseMove((e) => onMouseMove(e, 'before'))
  modifiedViewer?.onMouseMove((e) => onMouseMove(e, 'after'))
}

onMounted(async () => {
  pending.value++
  await createViewer(props.viewer)
  pending.value--
})
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
            :color="colors.before"
            size="small"
            text="before"
            @click="
              (e: PointerEvent) => {
                e.stopPropagation() // prevent @click of v-sheet in MainViewer
                useViewer().recreateViewer(viewer.id, {
                  type: 'file',
                  filePair: viewer.filePair,
                  category: 'before',
                })
              }
            "
          />
          <v-btn
            color="secondary"
            size="small"
            text="diff"
            @click="
              (e: PointerEvent) => {
                e.stopPropagation() // prevent @click of v-sheet in MainViewer
                useViewer().recreateViewer(viewer.id, {
                  type: 'diff',
                  filePair: viewer.filePair,
                })
              }
            "
          />
          <v-btn
            :color="colors.after"
            size="small"
            text="after"
            @click="
              (e: PointerEvent) => {
                e.stopPropagation() // prevent @click of v-sheet in MainViewer
                useViewer().recreateViewer(viewer.id, {
                  type: 'file',
                  filePair: viewer.filePair,
                  category: 'after',
                })
              }
            "
          />
        </v-btn-group>
      </v-menu>

      <div
        v-if="viewer.type === 'file'"
        class="flex-shrink-1 mx-1"
        style="min-width: 0%"
      >
        <v-tooltip location="top center" origin="auto" :open-delay="500">
          <template #activator="{ props: tooltipProps }">
            <span
              v-bind="tooltipProps"
              class="text-shrink text-subtitle-2"
              :style="`background-color: ${colors[viewer.category]};`"
            >
              {{
                getFileName(
                  getFilePath(viewer.filePair.getPathPair(), viewer.category),
                )
              }}
            </span>
          </template>
          {{
            getFileName(
              getFilePath(viewer.filePair.getPathPair(), viewer.category),
            )
          }}
        </v-tooltip>
      </div>
      <div
        v-else
        class="flex-shrink-1 mx-1 d-flex align-center flex-nowrap"
        style="min-width: 0%"
      >
        <v-tooltip location="top center" origin="auto" :open-delay="500">
          <template #activator="{ props: tooltipProps }">
            <span
              v-bind="tooltipProps"
              class="text-shrink text-subtitle-2"
              :style="`background-color: ${colors.before}`"
            >
              {{ getPathDifference(viewer.filePair.getPathPair())[0] }}
            </span>
          </template>
          {{ getPathDifference(viewer.filePair.getPathPair())[0] }}
        </v-tooltip>
        <v-icon
          size="small"
          icon="$mdiArrowRightBoldBox"
          color="purple"
          style="min-width: max-content"
        />
        <v-tooltip location="top center" origin="auto" :open-delay="500">
          <template #activator="{ props: tooltipProps }">
            <span
              v-bind="tooltipProps"
              class="text-shrink text-subtitle-2"
              :style="`background-color: ${colors.after}`"
            >
              {{ getPathDifference(viewer.filePair.getPathPair())[1] }}
            </span>
          </template>
          {{ getPathDifference(viewer.filePair.getPathPair())[1] }}
        </v-tooltip>
      </div>
      <v-tooltip location="top center" origin="auto" :open-delay="500">
        <template #activator="{ props: tooltipProps }">
          <v-btn
            v-bind="tooltipProps"
            variant="plain"
            density="compact"
            :icon="
              isOpeningFileList
                ? '$mdiArrowUpDropCircleOutline'
                : '$mdiArrowDownDropCircleOutline'
            "
            flat
            @click="() => (isOpeningFileList = !isOpeningFileList)"
          />
        </template>
        {{ isOpeningFileList ? 'Close' : 'Open' }} file list
      </v-tooltip>

      <span
        v-if="!canModify && (canModifyBefore || canModifyAfter)"
        class="text-shrink text-subtitle-2"
        :style="`border-bottom: 1px solid ${colors.info}; color: ${colors.info}`"
      >
        <v-icon
          size="small"
          icon="$mdiSourceCommitLocal"
          color="info"
          style="min-width: max-content; align-self: center"
        />
        You can modify
        <v-btn
          v-if="canModifyAfter"
          :color="colors.after"
          flat
          size="x-small"
          text="after"
          class="mx-1"
          @click="
            (e: PointerEvent) => {
              e.stopPropagation() // prevent @click of v-sheet in MainViewer
              useViewer().createViewer(
                {
                  type: 'file',
                  filePair: viewer.filePair,
                  category: 'after',
                },
                'next',
              )
            }
          "
        />
        <v-btn
          v-if="canModifyBefore"
          :color="colors.before"
          flat
          size="x-small"
          text="before"
          class="mx-1"
          @click="
            (e: PointerEvent) => {
              e.stopPropagation() // prevent @click of v-sheet in MainViewer
              useViewer().createViewer(
                {
                  type: 'file',
                  filePair: viewer.filePair,
                  category: 'before',
                },
                'prev',
              )
            }
          "
        />source code</span
      >
      <span
        v-if="canModify"
        class="text-shrink text-subtitle-2"
        :style="`border-bottom: 1px solid ${colors.info}; color: ${colors.info}`"
      >
        <v-icon
          size="small"
          icon="$mdiSourceCommitLocal"
          color="info"
          style="min-width: max-content; align-self: center"
        />
        <span class="font-weight-bold">[Modifiable]</span>
        <v-btn
          color="info"
          flat
          size="x-small"
          class="mx-1"
          @click="
            async () => {
              if (viewer.type === 'diff') return
              const { annotationId } = useAnnotation().currentIds.value
              if (!annotationId) return
              const pathPair = viewer.filePair.getPathPair()
              const filePath =
                viewer.category === 'before'
                  ? pathPair.notFound ??
                    // eslint-disable-next-line prettier/prettier
                  (pathPair.before ?? pathPair.after)
                  : pathPair.notFound ??
                    // eslint-disable-next-line prettier/prettier
                  (pathPair.after ?? pathPair.before)
              const fileContent =
                (viewer.category === 'before'
                  ? originalViewer
                  : modifiedViewer
                )?.getValue() ?? ''
              useAnnotation().updateAnnotation(
                {
                  ...(
                    await apis.snapshots.modifyTemporarySnapshot(annotationId, {
                      filePath,
                      fileContent,
                      isRemoved: false,
                    })
                  ).data,
                },
                true,
              )
            }
          "
          ><span class="text-none">Save Modification</span></v-btn
        ><v-btn
          v-if="
            (canModifyAfter && viewer.filePair.next?.isNotRemovedYet()) ||
            (canModifyBefore && viewer.filePair.isNotRemovedYet())
          "
          color="info"
          flat
          size="x-small"
          class="mx-1"
          @click="
            async () => {
              if (viewer.type === 'diff') return
              const { annotationId } = useAnnotation().currentIds.value
              if (!annotationId) return
              const pathPair = viewer.filePair.getPathPair()
              const filePath =
                viewer.category === 'before'
                  ? pathPair.notFound ??
                    // eslint-disable-next-line prettier/prettier
                  (pathPair.before ?? pathPair.after)
                  : pathPair.notFound ??
                    // eslint-disable-next-line prettier/prettier
                  (pathPair.after ?? pathPair.before)
              useAnnotation().updateAnnotation(
                {
                  ...(
                    await apis.snapshots.modifyTemporarySnapshot(annotationId, {
                      filePath,
                      fileContent: '',
                      isRemoved: true,
                    })
                  ).data,
                },
                true,
              )
            }
          "
          ><span class="text-none">Remove This File</span></v-btn
        >
      </span>

      <v-spacer />
      <v-divider v-if="navigator" vertical />
      <v-tooltip location="top center" origin="auto" :open-delay="500">
        <template #activator="{ props: tooltipProps }">
          <v-btn
            v-if="navigator"
            v-bind="tooltipProps"
            variant="plain"
            density="compact"
            icon="$mdiMenuLeftOutline"
            flat
            @click="
              (e: PointerEvent) => {
                e.stopPropagation() // prevent @click of v-sheet in MainViewer
                useViewer().navigate(viewer.id, 'prev')
              }
            "
          />
        </template>
        Show previous
      </v-tooltip>
      <v-tooltip
        v-if="navigator"
        location="top center"
        origin="auto"
        :open-delay="500"
      >
        <template #activator="{ props: tooltipProps }">
          <code
            v-bind="tooltipProps"
            class="text-shrink"
            style="
              max-width: 20%;
              border: 0.5px solid black;
              background-color: rgba(255, 250, 240, 0.7);
            "
            >{{ navigator.label }}</code
          >
        </template>
        <code>{{ navigator.label }}</code>
      </v-tooltip>
      <span v-if="navigator" class="text-body-2 ml-1">{{
        `${navigator.currentDestinationIndex + 1}/${
          navigator.destinations.length
        }`
      }}</span>
      <v-tooltip location="top center" origin="auto" :open-delay="500">
        <template #activator="{ props: tooltipProps }">
          <v-btn
            v-if="navigator"
            v-bind="tooltipProps"
            variant="plain"
            density="compact"
            :icon="'$mdiMenuRightOutline'"
            flat
            @click="
              (e: PointerEvent) => {
                e.stopPropagation() // prevent @click of v-sheet in MainViewer
                useViewer().navigate(viewer.id, 'next')
              }
            "
          />
        </template>
        Show next
      </v-tooltip>
      <v-tooltip location="top center" origin="auto" :open-delay="500">
        <template #activator="{ props: tooltipProps }">
          <v-btn
            v-if="navigator"
            v-bind="tooltipProps"
            variant="plain"
            density="compact"
            icon="$mdiCloseCircleOutline"
            flat
            class="mr-1"
            @click="
              () => {
                useViewer().deleteNavigator(viewer.id)
                // TODO: 他のところから切り替わった際を考える
                useCommonTokenSequence().updateSelectedId(undefined)
              }
            "
          />
        </template>
        Delete navigation
      </v-tooltip>
      <v-divider v-if="navigator" vertical />

      <v-tooltip location="top center" origin="auto" :open-delay="500">
        <template #activator="{ props: tooltipProps }">
          <v-btn
            v-if="useViewer().viewers.value.length > 1"
            v-bind="tooltipProps"
            variant="plain"
            density="compact"
            icon="$mdiTabRemove"
            flat
            @click="
              (e: PointerEvent) => {
                e.stopPropagation() // prevent @click of v-sheet in MainViewer
                useViewer().deleteViewer(viewer.id)
              }
            "
          />
        </template>
        Delete this window
      </v-tooltip>
      <v-tooltip location="top center" origin="auto" :open-delay="500">
        <template #activator="{ props: tooltipProps }">
          <v-btn
            v-bind="tooltipProps"
            variant="plain"
            density="compact"
            :icon="'$mdiTabPlus'"
            flat
            @click="
              (e: PointerEvent) => {
                e.stopPropagation() // prevent @click of v-sheet in MainViewer
                useViewer().duplicateViewer(viewer.id)
              }
            "
          />
        </template>
        Duplicate this window
      </v-tooltip>
    </div>
    <v-divider />
    <div class="flex-grow-1 position-relative">
      <v-expand-transition style="position: absolute; z-index: 100">
        <v-sheet v-if="isOpeningFileList" style="width: 100%; height: 100%">
          <h2 class="mx-1">Changed File List</h2>
          <div style="width: 100%; height: 100%; overflow-y: scroll">
            <v-list :opened="[`${viewer.id}:(Project Root)`]" class="py-0 mx-1"
              ><file-list
                :viewer-id="viewer.id"
                :file-tree="
                  getFileTreeStructure(
                    useAnnotation()
                      .getCurrentFilePairs()
                      .map((filePair) => {
                        if (filePair.status === 'not found')
                          return filePair.notFoundPath
                        if (filePair.status === 'added')
                          return filePair.after.path
                        return filePair.before.path
                      }),
                    '(Project Root)',
                  )
                "
                former-path=""
                :on-file-change="() => (isOpeningFileList = !isOpeningFileList)"
              />
            </v-list>
          </div>
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

.text-shrink {
  display: flex;
  overflow-x: hidden;
  white-space: nowrap;
}

::v-deep(.element-editor) {
  .file-changed-before {
    background: rgba(255, 98, 88, 0.5);
  }
  .file-changed-after {
    background: rgba(175, 208, 107, 0.5);
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
