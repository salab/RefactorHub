<script setup lang="ts">
import * as monaco from 'monaco-editor'
import { DiffCategory } from 'refactorhub'
import { CommonTokenSequenceDecorationManager } from './ts/commonTokensDecorations'
import { CodeFragmentManager } from './ts/codeFragments'
import { ElementDecorationManager } from './ts/elementDecorations'
import { ElementWidgetManager } from './ts/elementWidgets'
import { logger } from '@/utils/logger'
import { FileViewer } from '@/composables/useViewer'

const props = defineProps({
  viewer: {
    type: Object as () => FileViewer,
    required: true,
  },
})

const isDividingChangeAfter = computed(
  () =>
    useAnnotation().isDividingChange.value &&
    props.viewer.navigation.category === 'after',
)

function getFilePair(viewer?: FileViewer) {
  if (isDividingChangeAfter.value) {
    const filePair2 = viewer ? viewer.filePair.next : props.viewer.filePair.next
    if (!filePair2) {
      throw new Error(
        `Cannot find the filePair2: path is ${
          props.viewer.filePair.getPathPair().after
        }`,
      )
    }
    return filePair2
  }
  return viewer ? viewer.filePair : props.viewer.filePair
}

// eslint-disable-next-line vue/no-setup-props-destructure
const originalCategory = props.viewer.navigation.category

const pending = ref(0)
const isLoading = computed(() => pending.value > 0)

const isOpeningFileList = ref(false)

const { mainViewerId, getNavigator } = useViewer()
const navigator = getNavigator(props.viewer.id)

let fileViewer: monaco.editor.IStandaloneCodeEditor | undefined
let changedLineDecorationsCollection:
  | monaco.editor.IEditorDecorationsCollection
  | undefined

function updateChangedLineDecorations() {
  changedLineDecorationsCollection?.clear()
  const changedRanges: monaco.Range[] = []
  for (const { before, after } of getFilePair().diffHunks) {
    if (props.viewer.navigation.category === 'before' && before) {
      changedRanges.push(
        new monaco.Range(before.startLine, 1, before.endLine, 1),
      )
    }
    if (props.viewer.navigation.category === 'after' && after) {
      changedRanges.push(new monaco.Range(after.startLine, 1, after.endLine, 1))
    }
  }
  changedLineDecorationsCollection = fileViewer?.createDecorationsCollection(
    changedRanges.map((range) => ({
      range,
      options: {
        isWholeLine: true,
        className: `file-changed-${props.viewer.navigation.category}`,
      },
    })),
  )
}
function navigate(viewer: FileViewer) {
  const range = viewer.navigation.range
  if (!range) return
  useViewer().updateViewer(viewer.id, {
    ...viewer,
    navigation: { ...viewer.navigation, range: undefined },
  })
  setTimeout(() => {
    fileViewer?.revealRangeAtTop(range)
  }, 100)
}

function createFileViewer(
  container: HTMLElement,
  viewer: FileViewer,
): {
  diffViewer?: undefined
  originalViewer?: monaco.editor.IStandaloneCodeEditor
  modifiedViewer?: monaco.editor.IStandaloneCodeEditor
} {
  const filePair = getFilePair(viewer)
  const { navigation } = viewer
  const { category } = navigation
  fileViewer = monaco.editor.create(container, {
    automaticLayout: true,
    readOnly: true,
    renderWhitespace: 'all',
  })
  const textModel = useAnnotation().getTextModel(filePair, category)
  fileViewer.setModel(textModel)

  updateChangedLineDecorations()

  navigate(viewer)

  if (category === 'before')
    return {
      originalViewer: fileViewer,
    }
  return {
    modifiedViewer: fileViewer,
  }
}

let latestMousePosition: monaco.Position | undefined

async function createViewer(viewer: FileViewer) {
  const container = document.getElementById(viewer.id)
  if (!container) {
    logger.error(`Cannot find the container element: id is ${viewer.id}`)
    return
  }
  const filePair = getFilePair(viewer)

  const viewers = await createFileViewer(container, props.viewer)
  const originalViewer = viewers.originalViewer
  const modifiedViewer = viewers.modifiedViewer

  const elementDecorationManager = !isDividingChangeAfter.value
    ? new ElementDecorationManager(
        filePair.getPathPair(),
        originalViewer,
        modifiedViewer,
      )
    : undefined
  const codeFragmentManager = !isDividingChangeAfter.value
    ? new CodeFragmentManager(
        filePair.getPathPair(),
        filePair.before?.elements ?? [],
        filePair.after?.elements ?? [],
        originalViewer,
        modifiedViewer,
      )
    : undefined
  const elementWidgetManager = !isDividingChangeAfter.value
    ? new ElementWidgetManager(
        filePair.before?.elements ?? [],
        filePair.after?.elements ?? [],
        originalViewer,
        modifiedViewer,
      )
    : undefined
  const commonTokenSequenceDecorationManager = !isDividingChangeAfter.value
    ? new CommonTokenSequenceDecorationManager(
        filePair.getPathPair(),
        originalViewer,
        modifiedViewer,
      )
    : undefined

  watch(
    () => props.viewer,
    (newViewer) => {
      if (newViewer.navigation.category !== originalCategory) {
        useViewer().recreateViewer(newViewer.id)
        return
      }
      const filePair = getFilePair(newViewer)
      const text = filePair[newViewer.navigation.category]?.text
      if (text !== undefined) fileViewer?.setValue(text)
      updateChangedLineDecorations()
      elementDecorationManager?.update(filePair.getPathPair())
      codeFragmentManager?.update(
        filePair.getPathPair(),
        filePair.before?.elements ?? [],
        filePair.after?.elements ?? [],
      )
      elementWidgetManager?.update(
        filePair.before?.elements ?? [],
        filePair.after?.elements ?? [],
      )
      commonTokenSequenceDecorationManager?.update(filePair.getPathPair())
      navigate(newViewer)
    },
  )

  function onMouseDown(
    e: monaco.editor.IEditorMouseEvent,
    category: DiffCategory,
  ) {
    if (e.target.type !== monaco.editor.MouseTargetType.CONTENT_WIDGET) return
    if (isDividingChangeAfter.value || !commonTokenSequenceDecorationManager)
      return
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
        ? getFilePair().before?.path
        : getFilePair().after?.path
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
      !useAnnotation().isDividingChange.value || otherCategory === 'before'
        ? {
            type: 'file',
            filePair: filePairOnOtherViewer,
            navigation: {
              category: otherCategory,
              range: sequencesOnOtherViewer[0].range,
            },
          }
        : {
            type: 'diff',
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
  originalViewer?.onMouseDown((e) => onMouseDown(e, 'before'))
  modifiedViewer?.onMouseDown((e) => onMouseDown(e, 'after'))

  function onMouseMove(
    e: monaco.editor.IEditorMouseEvent,
    category: DiffCategory,
  ) {
    if (e.target.position) latestMousePosition = e.target.position
    if (e.target.type !== monaco.editor.MouseTargetType.CONTENT_TEXT) return
    if (isDividingChangeAfter.value) return
    const path = getFilePair().getPathPair()[category]
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
            v-if="viewer.navigation.category === 'before'"
            :color="colors.before"
            flat
            size="x-small"
            text="before"
            class="mx-1"
            v-bind="menuProps"
          />
          <v-btn
            v-if="viewer.navigation.category === 'after'"
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

      <div class="flex-shrink-1 d-flex mx-1" style="min-width: 0%">
        <v-tooltip location="top center" origin="auto" :open-delay="500">
          <template #activator="{ props: tooltipProps }">
            <span
              v-bind="tooltipProps"
              class="text-shrink text-subtitle-2"
              :style="`background-color: ${
                colors[viewer.navigation.category]
              };`"
            >
              {{
                getFileName(
                  getFilePath(
                    getFilePair().getPathPair(),
                    viewer.navigation.category,
                  ),
                )
              }}
            </span>
          </template>
          {{
            getFileName(
              getFilePath(
                getFilePair().getPathPair(),
                viewer.navigation.category,
              ),
            )
          }}
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
