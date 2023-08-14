<script setup lang="ts">
import * as monaco from 'monaco-editor'
import { CommonTokens } from './ts/commonTokensDecorations'
import { setTextModelOnDiffEditor } from './ts/displayedFile'
import MonacoEditor from '@/components/common/editor/MonacoEditor.vue'
import { logger } from '@/utils/logger'

function createCommonTokensDecoration(
  count: number,
  commonTokens: CommonTokens,
): monaco.editor.IModelDeltaDecoration {
  const level = count < 3 ? 1 : 2
  return {
    range: commonTokens.range,
    options: {
      className: `commonTokens-decoration-hovered-${level}`,
    },
  }
}

const props = defineProps({
  count: {
    type: Number as () => number,
    required: true,
  },
  commonTokens: {
    type: Object as () => CommonTokens,
    required: true,
  },
  closePopup: {
    type: Function as unknown as () => () => void,
    required: true,
  },
})

const fileIndex = computed(
  () =>
    useDraft()
      .commit.value?.files?.map((f) =>
        props.commonTokens.category === 'before' ? f.previousName : f.name,
      )
      ?.indexOf(props.commonTokens.path || ''),
)

const commonTokensEditorRef = ref<InstanceType<typeof MonacoEditor>>()
onMounted(() => {
  const diffEditor = commonTokensEditorRef.value?.getDiffEditor()
  const computeDiffWrapper = commonTokensEditorRef.value?.computeDiffWrapper
  if (!diffEditor || !computeDiffWrapper) {
    logger.log('commonTokensEditor is not loaded')
    return
  }
  const index = fileIndex.value
  if (index === undefined) {
    logger.log('fileIndex is not loaded')
    return
  }

  setTextModelOnDiffEditor('before', { index }, diffEditor, computeDiffWrapper)
  setTextModelOnDiffEditor('after', { index }, diffEditor, computeDiffWrapper)

  if (props.commonTokens.category === 'after') {
    commonTokensEditorRef.value?.$el.classList.add('x-50')
  }

  const disposables: monaco.IDisposable[] = []
  disposables.push(
    diffEditor.onDidUpdateDiff(() => {
      const editor =
        props.commonTokens.category === 'before'
          ? diffEditor.getOriginalEditor()
          : diffEditor.getModifiedEditor()
      editor.deltaDecorations(
        [],
        [createCommonTokensDecoration(props.count, props.commonTokens)],
      )
      setTimeout(
        () => editor.revealRangeInCenter(props.commonTokens.range),
        100,
      )

      disposables.forEach((it) => it.dispose())
    }),
  )
})

const openLocation = () => {
  const index = fileIndex.value
  if (index !== undefined && index >= 0) {
    const file = {
      index,
      lineNumber: props.commonTokens.range.startLineNumber,
    }
    useDraft().displayedFile.value.before =
      props.commonTokens.category === 'before' ? file : { index }
    useDraft().displayedFile.value.after =
      props.commonTokens.category === 'after' ? file : { index }
    props.closePopup()
  }
}
</script>

<template>
  <div>
    <v-card-text class="pb-0">
      <v-btn
        variant="text"
        size="x-small"
        icon="$mdiEyeOutline"
        title="Preview on editor"
        @click="openLocation"
      />
      {{ commonTokens.path }} {{ commonTokens.range.startLineNumber }}:{{
        commonTokens.range.startColumn
      }}~{{ commonTokens.range.endLineNumber }}:{{
        commonTokens.range.endColumn
      }}
    </v-card-text>
    <div class="container">
      <monaco-editor ref="commonTokensEditorRef" class="element-editor" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.container {
  min-width: 200%;
  overflow-x: hidden;
  height: 200px;
  padding: 0%;
  transform: scale(0.8);
}
.x-50 {
  transform: translateX(-50%);
}
::v-deep(.element-editor) {
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
