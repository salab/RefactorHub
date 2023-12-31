<script setup lang="ts">
import * as monaco from 'monaco-editor'
import { DiffCategory } from 'refactorhub'
import { CommonTokenSequenceDecorationManager } from './ts/commonTokensDecorations'
import { CodeFragmentManager } from './ts/codeFragments'
import { ElementDecorationManager } from './ts/elementDecorations'
import { ElementWidgetManager } from './ts/elementWidgets'
import { logger } from '@/utils/logger'
import { DiffViewer } from '@/composables/useViewer'
import { DiffHunk } from 'apis'

const props = defineProps({
  viewer: {
    type: Object as () => DiffViewer,
    required: true,
  },
})

const isOpeningFileList = ref(false)

const { mainViewerId, getNavigator } = useViewer()
const navigator = getNavigator(props.viewer.id)

function calculateDiff(diffHunks: DiffHunk[]) {
  const changes: monaco.editor.LineRangeMapping[] = []
  let beforeLineNumber = 0
  let afterLineNumber = 0
  for (const { before, after } of diffHunks) {
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
  return changes
}

function createDiffViewer(
  container: HTMLElement,
  viewer: DiffViewer,
): {
  diffViewer: monaco.editor.IStandaloneDiffEditor
  originalViewer: monaco.editor.IStandaloneCodeEditor
  modifiedViewer: monaco.editor.IStandaloneCodeEditor
} {
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
            identical: props.viewer.filePair.diffHunks.length === 0,
            quitEarly: false,
            changes: calculateDiff(props.viewer.filePair.diffHunks),
            moves: [],
          }),
        ),
    },
  })

  const { filePair } = viewer
  const originalTextModel = useAnnotation().getTextModel(filePair, 'before')
  const modifiedTextModel = useAnnotation().getTextModel(filePair, 'after')
  diffViewer.setModel({
    original: originalTextModel,
    modified: modifiedTextModel,
  })

  const originalViewer = diffViewer.getOriginalEditor()
  const modifiedViewer = diffViewer.getModifiedEditor()

  return {
    diffViewer,
    originalViewer,
    modifiedViewer,
  }
}

let latestMousePosition: monaco.Position | undefined
let originalViewer: monaco.editor.IStandaloneCodeEditor | undefined
let modifiedViewer: monaco.editor.IStandaloneCodeEditor | undefined

function navigate(viewer: DiffViewer) {
  const navigation = viewer.navigation
  if (!navigation) return
  useViewer().updateViewer(viewer.id, {
    ...viewer,
    navigation: undefined,
  })
  const { category, range } = navigation
  const fileViewer = category === 'before' ? originalViewer : modifiedViewer
  setTimeout(() => fileViewer?.revealRangeAtTop(range), 500)
}

function createViewer(viewer: DiffViewer) {
  const container = document.getElementById(viewer.id)
  if (!container) {
    logger.error(`Cannot find the container element: id is ${viewer.id}`)
    return
  }
  const { filePair } = viewer

  const viewers = createDiffViewer(container, props.viewer)
  originalViewer = viewers.originalViewer
  modifiedViewer = viewers.modifiedViewer

  navigate(viewer)

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
    () => props.viewer,
    (newViewer) => {
      if (newViewer.filePair.before)
        originalViewer?.setValue(newViewer.filePair.before.text)
      if (newViewer.filePair.after)
        modifiedViewer?.setValue(newViewer.filePair.after.text)
      navigate(newViewer)
      elementDecorationManager.update(newViewer.filePair.getPathPair())
      codeFragmentManager.update(
        newViewer.filePair.getPathPair(),
        newViewer.filePair.before?.elements ?? [],
        newViewer.filePair.after?.elements ?? [],
      )
      elementWidgetManager.update(
        newViewer.filePair.before?.elements ?? [],
        newViewer.filePair.after?.elements ?? [],
      )
      commonTokenSequenceDecorationManager.update(
        newViewer.filePair.getPathPair(),
      )
    },
  )

  originalViewer?.onMouseDown((e) =>
    onMouseDown(e, 'before', commonTokenSequenceDecorationManager),
  )
  modifiedViewer?.onMouseDown((e) =>
    onMouseDown(e, 'after', commonTokenSequenceDecorationManager),
  )

  originalViewer?.onMouseMove((e) => onMouseMove(e, 'before'))
  modifiedViewer?.onMouseMove((e) => onMouseMove(e, 'after'))
}

function onMouseDown(
  e: monaco.editor.IEditorMouseEvent,
  category: DiffCategory,
  commonTokenSequenceDecorationManager: CommonTokenSequenceDecorationManager,
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
    category === 'before'
      ? props.viewer.filePair.before?.path
      : props.viewer.filePair.after?.path
  if (currentPath === undefined)
    throw new Error('cannot get currentPath of viewer')
  const currentDestinationIndex = sequencesOnThisViewer.findIndex(
    (sequence) =>
      latestMousePosition &&
      sequence.isIn(currentPath, category) &&
      sequence.range.containsPosition(latestMousePosition),
  )
  useViewer().deleteNavigators()
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
  const sequencesOnOtherViewer = tokenSequenceSet.filterCategory(otherCategory)
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
function onMouseMove(
  e: monaco.editor.IEditorMouseEvent,
  category: DiffCategory,
) {
  if (e.target.position) latestMousePosition = e.target.position
  if (e.target.type !== monaco.editor.MouseTargetType.CONTENT_TEXT) return
  const path = props.viewer.filePair.getPathPair()[category]
  if (path === undefined) return
  useCommonTokenSequence().updateIsHovered(path, category, e.target.position)
}

onMounted(() => {
  createViewer(props.viewer)
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
            color="secondary"
            flat
            size="x-small"
            text="diff"
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
                useViewer().updateViewer(viewer.id, {
                  type: 'file',
                  filePair: viewer.filePair,
                  navigation: {
                    category: 'before',
                  },
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
                useViewer().updateViewer(viewer.id, {
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
                useViewer().updateViewer(viewer.id, {
                  type: 'file',
                  filePair: viewer.filePair,
                  navigation: {
                    category: 'after',
                  },
                })
              }
            "
          />
        </v-btn-group>
      </v-menu>

      <div
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
        Show previous common token sequence
      </v-tooltip>
      <v-tooltip
        v-if="navigator"
        location="top center"
        origin="auto"
        :open-delay="500"
      >
        <template #activator="{ props: tooltipProps }">
          <span v-if="navigator" v-bind="tooltipProps" class="text-body-2"
            ><u>
              {{
                `${navigator.currentDestinationIndex + 1}/${
                  navigator.destinations.length
                }`
              }}</u
            ></span
          >
        </template>
        <div class="text-subtitle-2">
          Search Result of Common Token Sequence
        </div>
        <cite
          ><code>{{ navigator.label }}</code></cite
        >
      </v-tooltip>
      <v-tooltip location="top center" origin="auto" :open-delay="500">
        <template #activator="{ props: tooltipProps }">
          <v-btn
            v-if="navigator"
            v-bind="tooltipProps"
            variant="plain"
            density="compact"
            icon="$mdiMenuRightOutline"
            flat
            @click="
              (e: PointerEvent) => {
                e.stopPropagation() // prevent @click of v-sheet in MainViewer
                useViewer().navigate(viewer.id, 'next')
              }
            "
          />
        </template>
        Show next common token sequence
      </v-tooltip>
      <v-btn
        v-if="navigator"
        variant="plain"
        density="compact"
        icon="$mdiCloseCircleOutline"
        flat
        class="mr-1"
        @click="
          () => {
            useViewer().deleteNavigators()
            useCommonTokenSequence().updateSelectedId(undefined)
          }
        "
      />
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
        <file-list-sheet
          :is-opening-file-list="isOpeningFileList"
          :viewer-id="viewer.id"
          :on-file-change="() => (isOpeningFileList = !isOpeningFileList)"
        />
      </v-expand-transition>
      <div :id="viewer.id" class="wh-100 element-editor" />
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
  text-overflow: ellipsis;
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
