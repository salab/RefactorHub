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

const { id, navigator } = props.viewer

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
  if (!file) throw new Error('commit file is not found')

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
  if (!file) throw new Error('commit file is not found')

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

onMounted(async () => {
  const container = document.getElementById(id)
  if (!container) {
    logger.error(`Cannot find the container element: id is ${id}`)
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
})
</script>

<template>
  <div class="d-flex flex-column fill-height">
    <div class="d-flex align-center">
      {{ viewer.id }}
      <!-- {{
        viewer.type === 'file'
          ? viewer.path
          : `${viewer.beforePath} → ${viewer.afterPath}`
      }} -->
    </div>
    <v-divider />
    <div class="flex-grow-1 position-relative">
      <div :id="id" class="wh-100 element-editor">
        <loading-circle :active="isLoading" />
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.position-relative {
  position: relative;
}
.wh-100 {
  width: 100%;
  height: 100%;
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
