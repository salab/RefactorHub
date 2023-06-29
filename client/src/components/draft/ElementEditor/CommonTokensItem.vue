<template>
  <div>
    <v-card-text class="pb-0">
      <v-btn
        x-small
        icon
        title="Preview on editor"
        color="secondary"
        @click="openLocation"
      >
        <v-icon x-small>fa-fw fa-eye</v-icon>
      </v-btn>
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

<script lang="ts">
import * as monaco from 'monaco-editor'
import MonacoEditor from '@/components/common/editor/MonacoEditor.vue'
import {
  defineComponent,
  computed,
  useContext,
  ref,
  onMounted,
} from '@nuxtjs/composition-api'
import { logger } from '@/utils/logger'
import { CommonTokens } from './ts/commonTokensDecorations'
import { setTextModelOnDiffEditor } from './ts/displayedFile'

function createCommonTokensDecoration(
  count: number,
  commonTokens: CommonTokens
): monaco.editor.IModelDeltaDecoration {
  const level = count < 3 ? 1 : 2
  return {
    range: commonTokens.range,
    options: {
      className: `commonTokens-decoration commonTokens-decoration-${level}`,
    },
  }
}

export default defineComponent({
  props: {
    count: {
      type: Number as () => number,
      required: true,
    },
    commonTokens: {
      type: Object as () => CommonTokens,
      required: true,
    },
    closePopup: {
      type: (Function as unknown) as () => () => void,
      required: true,
    },
  },
  setup(props) {
    const {
      app: { $accessor },
    } = useContext()

    const fileIndex = computed(() =>
      $accessor.draft.commit?.files
        ?.map((f) =>
          props.commonTokens.category === 'before' ? f.previousName : f.name
        )
        ?.indexOf(props.commonTokens.path || '')
    )

    const commonTokensEditorRef = ref<InstanceType<typeof MonacoEditor>>()
    onMounted(() => {
      const diffEditor = commonTokensEditorRef.value?.diffEditor
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

      setTextModelOnDiffEditor(
        'before',
        { index },
        diffEditor,
        computeDiffWrapper,
        $accessor
      )
      setTextModelOnDiffEditor(
        'after',
        { index },
        diffEditor,
        computeDiffWrapper,
        $accessor
      )

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
            [createCommonTokensDecoration(props.count, props.commonTokens)]
          )
          setTimeout(
            () => editor.revealRangeInCenter(props.commonTokens.range),
            100
          )

          disposables.forEach((it) => it.dispose())
        })
      )
    })

    const openLocation = async () => {
      const index = fileIndex.value
      if (index !== undefined && index >= 0) {
        const file = {
          index,
          lineNumber: props.commonTokens.range.startLineNumber,
        }
        await $accessor.draft.setDisplayedFile({
          category: 'before',
          file: props.commonTokens.category === 'before' ? file : { index },
        })
        await $accessor.draft.setDisplayedFile({
          category: 'after',
          file: props.commonTokens.category === 'after' ? file : { index },
        })
        props.closePopup()
      }
    }

    return { commonTokensEditorRef, openLocation }
  },
})
</script>

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
.element-editor ::v-deep {
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
