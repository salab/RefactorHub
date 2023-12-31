<script setup lang="ts">
import * as monaco from 'monaco-editor'
import { DiffCategory } from 'refactorhub'
import { debounce } from 'lodash-es'
import { CommonTokenSequenceDecorationManager } from './ts/commonTokensDecorations'
import { CodeFragmentManager } from './ts/codeFragments'
import { ElementDecorationManager } from './ts/elementDecorations'
import { ElementWidgetManager } from './ts/elementWidgets'
import { logger } from '@/utils/logger'
import { DiffViewer } from '@/composables/useViewer'
import { FilePair } from '@/composables/useAnnotation'
import apis from '@/apis'

const AUTO_INSERTED_LINE_CONTENT =
  '// Do Not Modify This Line; RefactorHub Inserted\n'

const props = defineProps({
  viewer: {
    type: Object as () => DiffViewer,
    required: true,
  },
})

const scrollTop = ref(0)
const scrollLeft = ref(0)

const isOpeningFileList = ref(false)

const { mainViewerId, getNavigator } = useViewer()
const navigator = getNavigator(props.viewer.id)

let latestMousePosition: monaco.Position | undefined
let originalViewer: monaco.editor.IStandaloneCodeEditor | undefined
let intermediateViewer: monaco.editor.IStandaloneCodeEditor | undefined
let modifiedViewer: monaco.editor.IStandaloneCodeEditor | undefined

function navigate(
  viewer: DiffViewer,
  beforeLinesMap: {
    [lineNumber: number]: number
  },
  intermediateLinesMap: {
    [lineNumber: number]: number
  },
) {
  const navigation = viewer.navigation
  if (!navigation) return
  useViewer().updateViewer(viewer.id, {
    ...viewer,
    navigation: undefined,
  })
  const { category, range } = navigation
  let { startLineNumber, endLineNumber } = range
  const linesMap = category === 'before' ? beforeLinesMap : intermediateLinesMap
  startLineNumber = linesMap[startLineNumber]
  endLineNumber = linesMap[endLineNumber]
  const mappedRange = new monaco.Range(
    startLineNumber,
    range.startColumn,
    endLineNumber,
    range.endColumn,
  )
  const fileViewer = category === 'before' ? originalViewer : intermediateViewer
  setTimeout(() => fileViewer?.revealRangeAtTop(mappedRange), 250)
}

let beforeLineDecorationsCollection:
  | monaco.editor.IEditorDecorationsCollection
  | undefined
let intermediateLineDecorationsCollection:
  | monaco.editor.IEditorDecorationsCollection
  | undefined
let afterLineDecorationsCollection:
  | monaco.editor.IEditorDecorationsCollection
  | undefined

function updateDiff(filePair1: FilePair, filePair2: FilePair) {
  beforeLineDecorationsCollection?.clear()
  intermediateLineDecorationsCollection?.clear()
  afterLineDecorationsCollection?.clear()

  const beforeText = filePair1.before?.text ?? ''
  const intermediateText = filePair1.after?.text ?? ''
  const afterText = filePair2.after?.text ?? ''

  const diffHunks1 = filePair1.diffHunks
  const diffHunks2 = filePair2.diffHunks

  const beforeLines: {
    [lineNumber: number]: { followingEmptyLines: number; isRemoved: boolean }
  } = {}
  for (let l = 0; l < beforeText.split('\n').length + 1; l++) {
    beforeLines[l] = { followingEmptyLines: 0, isRemoved: false }
  }
  const intermediateLines: {
    [lineNumber: number]: {
      followingEmptyLines: number
      isAdded: boolean
      isRemoved: boolean
      correspondingBeforeLines: number[]
      correspondingAfterLines: number[]
    }
  } = {}
  let correspondingBeforeLine = 0
  let correspondingAfterLine = 0
  for (let l = 0; l < intermediateText.split('\n').length + 1; l++) {
    const diffHunk1 = diffHunks1.find(({ before, after }) => {
      if (before && before.oppositeLine === l) return true
      if (after) return after.startLine <= l && l <= after.endLine
      return false
    })
    const correspondingBeforeLines: number[] = []
    if (diffHunk1) {
      const { before, after } = diffHunk1
      if (before && before.oppositeLine === l) {
        if (after) {
          correspondingBeforeLines.push(correspondingBeforeLine)
          correspondingBeforeLine++
        } else {
          for (
            let beforeL = correspondingBeforeLine;
            beforeL <= before.endLine;
            beforeL++
          ) {
            correspondingBeforeLines.push(beforeL)
          }
          correspondingBeforeLine = before.endLine + 1
        }
      } else if (after) {
        if (before) {
          if (after.endLine === l) {
            if (correspondingBeforeLine <= before.endLine) {
              for (
                let beforeL = correspondingBeforeLine;
                beforeL <= before.endLine;
                beforeL++
              ) {
                correspondingBeforeLines.push(beforeL)
              }
              correspondingBeforeLine = before.endLine + 1
            } else {
              correspondingBeforeLines.push(before.endLine)
              // updating correspondingBeforeLine is unnecessary
            }
          } else if (correspondingBeforeLine <= before.endLine) {
            correspondingBeforeLines.push(correspondingBeforeLine)
            correspondingBeforeLine++
          } else {
            correspondingBeforeLines.push(before.endLine)
            // updating correspondingBeforeLine is unnecessary
          }
        } else {
          correspondingBeforeLines.push(after.oppositeLine)
          // updating correspondingBeforeLine is unnecessary
        }
      }
    } else {
      correspondingBeforeLines.push(correspondingBeforeLine)
      correspondingBeforeLine++
    }

    const diffHunk2 = diffHunks2.find(({ before, after }) => {
      if (after && after.oppositeLine === l) return true
      if (before) return before.startLine <= l && l <= before.endLine
      return false
    })
    const correspondingAfterLines: number[] = []
    if (diffHunk2) {
      const { before, after } = diffHunk2
      if (after && after.oppositeLine === l) {
        if (before) {
          correspondingAfterLines.push(correspondingAfterLine)
          correspondingAfterLine++
        } else {
          for (
            let afterL = correspondingAfterLine;
            afterL <= after.endLine;
            afterL++
          ) {
            correspondingAfterLines.push(afterL)
          }
          correspondingAfterLine = after.endLine + 1
        }
      } else if (before) {
        if (after) {
          if (before.endLine === l) {
            if (correspondingAfterLine <= after.endLine) {
              for (
                let afterL = correspondingAfterLine;
                afterL <= after.endLine;
                afterL++
              ) {
                correspondingAfterLines.push(afterL)
              }
              correspondingAfterLine = after.endLine + 1
            } else {
              correspondingAfterLines.push(after.endLine)
              // updating correspondingAfterLine is unnecessary
            }
          } else if (correspondingAfterLine <= after.endLine) {
            correspondingAfterLines.push(correspondingAfterLine)
            correspondingAfterLine++
          } else {
            correspondingAfterLines.push(after.endLine)
            // updating correspondingAfterLine is unnecessary
          }
        } else {
          correspondingAfterLines.push(before.oppositeLine)
          // updating correspondingAfterLine is unnecessary
        }
      }
    } else {
      correspondingAfterLines.push(correspondingAfterLine)
      correspondingAfterLine++
    }

    intermediateLines[l] = {
      followingEmptyLines: 0,
      isAdded: false,
      isRemoved: false,
      correspondingBeforeLines,
      correspondingAfterLines,
    }
  }
  const afterLines: {
    [lineNumber: number]: { followingEmptyLines: number; isAdded: boolean }
  } = {}
  for (let l = 0; l < afterText.split('\n').length + 1; l++) {
    afterLines[l] = { followingEmptyLines: 0, isAdded: false }
  }

  diffHunks1.forEach(({ before, after }) => {
    if (before) {
      for (let l = before.startLine; l <= before.endLine; l++) {
        beforeLines[l].isRemoved = true
      }
    }
    if (after) {
      for (let l = after.startLine; l <= after.endLine; l++) {
        intermediateLines[l].isAdded = true
      }
    }
    const removedLines = !before ? 0 : before.endLine - before.startLine + 1
    const addedLines = !after ? 0 : after.endLine - after.startLine + 1
    if (removedLines < addedLines && after) {
      const beforeLine = after.oppositeLine + removedLines
      beforeLines[beforeLine].followingEmptyLines += addedLines - removedLines
    } else if (removedLines > addedLines && before) {
      const intermediateLine = before.oppositeLine + addedLines
      intermediateLines[intermediateLine].followingEmptyLines = Math.max(
        removedLines - addedLines,
        intermediateLines[intermediateLine].followingEmptyLines,
      )
      const { correspondingAfterLines } = intermediateLines[intermediateLine]
      afterLines[
        correspondingAfterLines[correspondingAfterLines.length - 1]
      ].followingEmptyLines += Math.max(
        removedLines - addedLines - correspondingAfterLines.length + 1,
        0,
      )
    }
  })

  diffHunks2.forEach(({ before, after }) => {
    if (before) {
      for (let l = before.startLine; l <= before.endLine; l++) {
        intermediateLines[l].isRemoved = true
      }
    }
    if (after) {
      for (let l = after.startLine; l <= after.endLine; l++) {
        afterLines[l].isAdded = true
      }
    }
    const removedLines = !before ? 0 : before.endLine - before.startLine + 1
    const addedLines = !after ? 0 : after.endLine - after.startLine + 1
    if (removedLines < addedLines && after) {
      const intermediateLine = after.oppositeLine + removedLines
      intermediateLines[intermediateLine].followingEmptyLines = Math.max(
        addedLines - removedLines,
        intermediateLines[intermediateLine].followingEmptyLines,
      )
      const { correspondingBeforeLines } = intermediateLines[intermediateLine]
      beforeLines[
        correspondingBeforeLines[correspondingBeforeLines.length - 1]
      ].followingEmptyLines += Math.max(
        addedLines - removedLines - correspondingBeforeLines.length + 1,
        0,
      )
    } else if (removedLines > addedLines && before) {
      const afterLine = before.oppositeLine + addedLines
      afterLines[afterLine].followingEmptyLines += removedLines - addedLines
    }
  })

  let newBeforeText = ''
  let newBeforeLine = 0
  const beforeLinesMap: { [originalBeforeLine: number]: number } = {}
  const newBeforeLines: {
    [newBeforeLine: number]: { type: 'unmodified' | 'removed' | 'empty' }
  } = {}
  for (let l = 0; l < beforeText.split('\n').length + 1; l++) {
    const text =
      l === 0
        ? ''
        : beforeText.split('\n')[l - 1] +
          (l === beforeText.split('\n').length ? '' : '\n')
    newBeforeText += text
    if (l !== 0) newBeforeLine++
    beforeLinesMap[l] = newBeforeLine
    newBeforeLines[newBeforeLine] = {
      type: beforeLines[l].isRemoved ? 'removed' : 'unmodified',
    }
    for (let i = 0; i < beforeLines[l].followingEmptyLines; i++) {
      if (newBeforeText && newBeforeText[newBeforeText.length - 1] !== '\n') {
        newBeforeText += '\n'
      }
      newBeforeText += '\n'
      newBeforeLine++
      newBeforeLines[newBeforeLine] = {
        type: 'empty',
      }
    }
  }
  let newIntermediateText = ''
  let newIntermediateLine = 0
  const intermediateLinesMap: { [originalIntermediateLine: number]: number } =
    {}
  const newIntermediateLines: {
    [newIntermediateLine: number]: {
      type: 'unmodified' | 'added' | 'removed' | 'both' | 'empty'
    }
  } = {}
  for (let l = 0; l < intermediateText.split('\n').length + 1; l++) {
    const text =
      l === 0
        ? ''
        : intermediateText.split('\n')[l - 1] +
          (l === intermediateText.split('\n').length ? '' : '\n')
    newIntermediateText += text
    if (l !== 0) newIntermediateLine++
    intermediateLinesMap[l] = newIntermediateLine
    newIntermediateLines[newIntermediateLine] = {
      type: intermediateLines[l].isAdded
        ? intermediateLines[l].isRemoved
          ? 'both'
          : 'added'
        : intermediateLines[l].isRemoved
        ? 'removed'
        : 'unmodified',
    }
    for (let i = 0; i < intermediateLines[l].followingEmptyLines; i++) {
      if (
        newIntermediateText &&
        newIntermediateText[newIntermediateText.length - 1] !== '\n'
      ) {
        newIntermediateText += '\n'
      }
      newIntermediateText += AUTO_INSERTED_LINE_CONTENT
      newIntermediateLine++
      newIntermediateLines[newIntermediateLine] = {
        type: 'empty',
      }
    }
  }
  let newAfterText = ''
  let newAfterLine = 0
  const afterLinesMap: { [originalAfterLine: number]: number } = {}
  const newAfterLines: {
    [newAfterLine: number]: { type: 'unmodified' | 'added' | 'empty' }
  } = {}
  for (let l = 0; l < afterText.split('\n').length + 1; l++) {
    const text =
      l === 0
        ? ''
        : afterText.split('\n')[l - 1] +
          (l === afterText.split('\n').length ? '' : '\n')
    newAfterText += text
    if (l !== 0) newAfterLine++
    afterLinesMap[l] = newAfterLine
    newAfterLines[newAfterLine] = {
      type: afterLines[l].isAdded ? 'added' : 'unmodified',
    }
    for (let i = 0; i < afterLines[l].followingEmptyLines; i++) {
      if (newAfterText && newAfterText[newAfterText.length - 1] !== '\n') {
        newAfterText += '\n'
      }
      newAfterText += '\n'
      newAfterLine++
      newAfterLines[newAfterLine] = {
        type: 'empty',
      }
    }
  }

  const oldScrollTop = scrollTop.value
  const oldScrollLeft = scrollLeft.value
  const oldOriginalViewerCursorPosition = originalViewer?.getPosition()
  const oldIntermediateViewerCursorPosition = intermediateViewer?.getPosition()
  const oldModifiedViewerCursorPosition = modifiedViewer?.getPosition()

  originalViewer?.setValue(newBeforeText)
  const intermediateSelections = intermediateViewer?.getSelections() ?? null
  if (intermediateViewer?.getValue() !== newIntermediateText) {
    intermediateViewer?.getModel()?.pushEditOperations(
      intermediateSelections,
      [
        {
          range:
            intermediateViewer.getModel()?.getFullModelRange() ??
            new monaco.Range(0, 0, 0, 0),
          text: newIntermediateText,
        },
      ],
      () => intermediateSelections,
    )
  }
  modifiedViewer?.setValue(newAfterText)

  originalViewer?.setScrollTop(oldScrollTop)
  originalViewer?.setScrollLeft(oldScrollLeft)
  intermediateViewer?.setScrollTop(oldScrollTop)
  intermediateViewer?.setScrollLeft(oldScrollLeft)
  modifiedViewer?.setScrollTop(oldScrollTop)
  modifiedViewer?.setScrollLeft(oldScrollLeft)
  if (oldOriginalViewerCursorPosition)
    originalViewer?.setPosition(oldOriginalViewerCursorPosition)
  if (oldIntermediateViewerCursorPosition)
    intermediateViewer?.setPosition(oldIntermediateViewerCursorPosition)
  if (oldModifiedViewerCursorPosition)
    modifiedViewer?.setPosition(oldModifiedViewerCursorPosition)

  beforeLineDecorationsCollection = originalViewer?.createDecorationsCollection(
    Object.entries(newBeforeLines)
      .filter(([, { type }]) => type !== 'unmodified')
      .map(([lineNumber, { type }]) => {
        const range = new monaco.Range(
          Number.parseInt(lineNumber),
          1,
          Number.parseInt(lineNumber),
          1,
        )
        const className =
          type === 'removed' ? 'file-changed-before' : 'file-changed-empty'
        return {
          range,
          options: {
            isWholeLine: true,
            className,
          },
        }
      }),
  )
  intermediateLineDecorationsCollection =
    intermediateViewer?.createDecorationsCollection(
      Object.entries(newIntermediateLines)
        .filter(([, { type }]) => type !== 'unmodified')
        .map(([lineNumber, { type }]) => {
          const range = new monaco.Range(
            Number.parseInt(lineNumber),
            1,
            Number.parseInt(lineNumber),
            1,
          )
          let className
          switch (type) {
            case 'added':
              className = 'file-changed-intermediate-after'
              break
            case 'removed':
              className = 'file-changed-intermediate-before'
              break
            case 'both':
              className = 'file-changed-intermediate-both'
              break
            default:
              className = 'file-changed-empty'
              break
          }
          return {
            range,
            options: {
              isWholeLine: true,
              className,
            },
          }
        }),
    )
  afterLineDecorationsCollection = modifiedViewer?.createDecorationsCollection(
    Object.entries(newAfterLines)
      .filter(([, { type }]) => type !== 'unmodified')
      .map(([lineNumber, { type }]) => {
        const range = new monaco.Range(
          Number.parseInt(lineNumber),
          1,
          Number.parseInt(lineNumber),
          1,
        )
        const className =
          type === 'added' ? 'file-changed-after' : 'file-changed-empty'
        return {
          range,
          options: {
            isWholeLine: true,
            className,
          },
        }
      }),
  )

  return {
    beforeLinesMap,
    intermediateLinesMap,
    afterLinesMap,
  }
}

function createViewer(viewer: DiffViewer) {
  const filePair1 = viewer.filePair
  const filePair2 = filePair1.next
  if (!filePair2) {
    logger.error(
      `Cannot find the filePair2: path is ${filePair1.getPathPair().after}`,
    )
    return
  }
  const containerBefore = document.getElementById(`${viewer.id}-before`)
  const containerIntermediate = document.getElementById(
    `${viewer.id}-intermediate`,
  )
  const containerAfter = document.getElementById(`${viewer.id}-after`)
  if (!containerBefore || !containerIntermediate || !containerAfter) {
    logger.error(`Cannot find the container element: id is ${viewer.id}`)
    return
  }

  originalViewer = monaco.editor.create(containerBefore, {
    automaticLayout: true,
    readOnly: true,
    renderWhitespace: 'all',
    lineNumbers: 'off',
    glyphMargin: true,
    folding: false,
    lineDecorationsWidth: 0,
    lineNumbersMinChars: 0,
    minimap: {
      enabled: false,
    },
  })
  originalViewer.setModel(useAnnotation().getTextModel(filePair1, 'before'))
  intermediateViewer = monaco.editor.create(containerIntermediate, {
    automaticLayout: true,
    readOnly: false,
    renderWhitespace: 'all',
    lineNumbers: 'off',
    glyphMargin: true,
    folding: false,
    lineDecorationsWidth: 0,
    lineNumbersMinChars: 0,
    minimap: {
      enabled: false,
    },
  })
  intermediateViewer.setModel(useAnnotation().getTextModel(filePair1, 'after'))
  modifiedViewer = monaco.editor.create(containerAfter, {
    automaticLayout: true,
    readOnly: true,
    renderWhitespace: 'all',
    lineNumbers: 'off',
    glyphMargin: true,
    folding: false,
    lineDecorationsWidth: 0,
    lineNumbersMinChars: 0,
    minimap: {
      enabled: false,
    },
  })
  modifiedViewer.setModel(useAnnotation().getTextModel(filePair2, 'after'))

  const { beforeLinesMap, intermediateLinesMap } = updateDiff(
    filePair1,
    filePair2,
  )

  originalViewer.onDidScrollChange((e) => {
    scrollTop.value = e.scrollTop
    scrollLeft.value = e.scrollLeft
  })
  intermediateViewer.onDidScrollChange((e) => {
    scrollTop.value = e.scrollTop
    scrollLeft.value = e.scrollLeft
  })
  modifiedViewer.onDidScrollChange((e) => {
    scrollTop.value = e.scrollTop
    scrollLeft.value = e.scrollLeft
  })
  watch(
    () => scrollTop.value,
    (newScrollTop) => {
      originalViewer?.setScrollTop(newScrollTop)
      intermediateViewer?.setScrollTop(newScrollTop)
      modifiedViewer?.setScrollTop(newScrollTop)
    },
  )
  watch(
    () => scrollLeft.value,
    (newScrollLeft) => {
      originalViewer?.setScrollLeft(newScrollLeft)
      intermediateViewer?.setScrollLeft(newScrollLeft)
      modifiedViewer?.setScrollLeft(newScrollLeft)
    },
  )

  navigate(viewer, beforeLinesMap, intermediateLinesMap)

  const elementDecorationManager = new ElementDecorationManager(
    filePair1.getPathPair(),
    originalViewer,
    intermediateViewer,
    beforeLinesMap,
    intermediateLinesMap,
  )
  const codeFragmentManager = new CodeFragmentManager(
    filePair1.getPathPair(),
    filePair1.before?.elements ?? [],
    filePair1.after?.elements ?? [],
    originalViewer,
    intermediateViewer,
    beforeLinesMap,
    intermediateLinesMap,
  )
  const elementWidgetManager = new ElementWidgetManager(
    filePair1.before?.elements ?? [],
    filePair1.after?.elements ?? [],
    originalViewer,
    intermediateViewer,
    beforeLinesMap,
    intermediateLinesMap,
  )
  const commonTokenSequenceDecorationManager =
    new CommonTokenSequenceDecorationManager(
      filePair1.getPathPair(),
      originalViewer,
      intermediateViewer,
      beforeLinesMap,
      intermediateLinesMap,
    )

  function update() {
    const filePair1 = props.viewer.filePair
    const filePair2 = filePair1.next
    if (!filePair2) {
      logger.error(
        `Cannot find the filePair2: path is ${filePair1.getPathPair().after}`,
      )
      return
    }
    const { beforeLinesMap, intermediateLinesMap } = updateDiff(
      filePair1,
      filePair2,
    )
    navigate(props.viewer, beforeLinesMap, intermediateLinesMap)
    elementDecorationManager.update(
      filePair1.getPathPair(),
      beforeLinesMap,
      intermediateLinesMap,
    )
    codeFragmentManager.update(
      filePair1.getPathPair(),
      filePair1.before?.elements ?? [],
      filePair1.after?.elements ?? [],
      beforeLinesMap,
      intermediateLinesMap,
    )
    elementWidgetManager.update(
      filePair1.before?.elements ?? [],
      filePair1.after?.elements ?? [],
      beforeLinesMap,
      intermediateLinesMap,
    )
    commonTokenSequenceDecorationManager.update(
      filePair1.getPathPair(),
      beforeLinesMap,
      intermediateLinesMap,
    )

    originalViewer?.onMouseMove((e) => onMouseMove(e, 'before', beforeLinesMap))
    intermediateViewer?.onMouseMove((e) =>
      onMouseMove(e, 'after', intermediateLinesMap),
    )
  }

  watch(
    () => props.viewer,
    () => update(),
  )

  intermediateViewer.onDidChangeModelContent(
    debounce(async () => {
      const filePair1 = props.viewer.filePair
      const filePair2 = filePair1.next
      if (!filePair2) {
        logger.error(
          `Cannot find the filePair2: path is ${filePair1.getPathPair().after}`,
        )
        return
      }
      const { annotationId } = useAnnotation().currentIds.value
      if (!annotationId) return
      const pathPair = filePair1.getPathPair()
      const filePath =
        pathPair.notFound ??
        // eslint-disable-next-line prettier/prettier
          (pathPair.after ?? pathPair.before)
      const fileContent =
        intermediateViewer
          ?.getValue()
          ?.replaceAll(AUTO_INSERTED_LINE_CONTENT, '') ?? ''
      const savedContent = filePair1.after?.text ?? ''
      if (fileContent === savedContent) {
        update()
        return
      }
      useLoader().setLoadingText('updating the intermediate source code')
      useViewer().deleteNavigators()
      useCommonTokenSequence().updateSelectedId(undefined)
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
      useLoader().finishLoading()
    }, 3000),
  )

  originalViewer?.onMouseDown((e) =>
    onMouseDown(e, 'before', commonTokenSequenceDecorationManager),
  )
  intermediateViewer?.onMouseDown((e) =>
    onMouseDown(e, 'after', commonTokenSequenceDecorationManager),
  )

  originalViewer?.onMouseMove((e) => onMouseMove(e, 'before', beforeLinesMap))
  intermediateViewer?.onMouseMove((e) =>
    onMouseMove(e, 'after', intermediateLinesMap),
  )
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
    otherCategory === 'before'
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
function onMouseMove(
  e: monaco.editor.IEditorMouseEvent,
  category: DiffCategory,
  linesMap: {
    [lineNumber: number]: number
  },
) {
  const position = e.target.position
  if (!position) return
  const oldLine = Object.entries(linesMap).find(
    ([, newLineNumber]) => newLineNumber === position.lineNumber,
  )
  if (!oldLine) return undefined
  const inverseLineNumber = Number.parseInt(oldLine[0])
  const inversePosition = new monaco.Position(
    inverseLineNumber,
    position.column,
  )
  latestMousePosition = inversePosition
  if (e.target.type !== monaco.editor.MouseTargetType.CONTENT_TEXT) return
  const path =
    category === 'before'
      ? props.viewer.filePair.before?.path
      : props.viewer.filePair.after?.path
  if (path === undefined) return
  useCommonTokenSequence().updateIsHovered(path, category, inversePosition)
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
              :style="`background-color: ${colors.intermediate}`"
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

      <v-btn
        v-if="viewer.filePair.next?.isNotRemovedYet()"
        color="info"
        flat
        size="x-small"
        class="mx-1"
        @click="
          async () => {
            const { annotationId } = useAnnotation().currentIds.value
            if (!annotationId) return
            const pathPair = viewer.filePair.getPathPair()
            const filePath =
              pathPair.notFound ??
              // eslint-disable-next-line prettier/prettier
                (pathPair.after ?? pathPair.before)
            useLoader().setLoadingText('updating the intermediate source code')
            useViewer().deleteNavigators()
            useCommonTokenSequence().updateSelectedId(undefined)
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
            useLoader().finishLoading()
          }
        "
        ><span class="text-none">Remove Intermediate File</span></v-btn
      >

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
      <div class="d-flex" style="width: 100%; height: 100%">
        <div
          :id="`${viewer.id}-before`"
          class="element-editor"
          :style="`width: ${100 / 3}%; height: 100%;`"
        />
        <div
          :id="`${viewer.id}-intermediate`"
          class="element-editor element-editor-modifiable"
          :style="`width: ${100 / 3}%; height: 100%;`"
        />
        <div
          :id="`${viewer.id}-after`"
          class="element-editor"
          :style="`width: ${100 / 3}%; height: 100%;`"
        />
      </div>
    </div>
  </v-sheet>
</template>

<style lang="scss" scoped>
.position-relative {
  position: relative;
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
  .file-changed-intermediate-before {
    background: linear-gradient(
      to right,
      rgba(0, 0, 0, 0),
      rgba(255, 165, 153, 0.8) calc(100% - 150px)
    );
  }
  .file-changed-intermediate-after {
    background: linear-gradient(
      to right,
      rgba(202, 218, 105, 0.5) 150px,
      rgba(0, 0, 0, 0)
    );
  }
  .file-changed-intermediate-both {
    background: linear-gradient(
      to right,
      rgba(202, 218, 105, 0.5) 150px,
      rgba(255, 165, 153, 0.8) calc(100% - 150px)
    );
  }
  .file-changed-empty {
    background: repeating-linear-gradient(
      -48deg,
      rgba(150, 150, 150, 0.5),
      rgba(150, 150, 150, 0.5) 1.5px,
      rgba(255, 255, 255, 0.5) 0,
      rgba(255, 255, 255, 0.5) 6px
    );
  }
  .glyph-margin {
    background: rgba(230, 230, 230, 0.5);
  }
}

::v-deep(.element-editor-modifiable) {
  .monaco-editor-background {
    background: rgba(201, 238, 255, 0.5);
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
