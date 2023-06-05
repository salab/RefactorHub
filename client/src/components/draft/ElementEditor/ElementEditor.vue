<template>
  <div class="d-flex flex-column fill-height">
    <code-fragment-diff />
    <div class="d-flex" style="height: 45px">
      <v-row no-gutters class="pa-2">
        <div class="mx-2">
          <v-btn
            depressed
            small
            width="60"
            class="text-none"
            :dark="lock.before"
            @click="lockEditor('before')"
          >
            {{ lock.before ? 'Unlock' : 'Lock' }}
          </v-btn>
        </div>
      </v-row>

      <v-switch
        v-model="showCommonTokens.value"
        label="Show Highlights of Common Tokens"
        color="indigo"
        dense
        class="my-2 pa-0"
      />

      <v-row no-gutters class="pa-2 flex-row-reverse">
        <div class="mx-2">
          <v-btn
            depressed
            small
            width="60"
            class="text-none"
            :dark="lock.after"
            @click="lockEditor('after')"
          >
            {{ lock.after ? 'Unlock' : 'Lock' }}
          </v-btn>
        </div>
      </v-row>
    </div>
    <v-divider />
    <div class="flex-grow-1 position-relative">
      <div class="common-token-popup-wrapper">
        <div v-show="commonTokensSelected.before" class="common-token-popup">
          <div class="inner">
            <v-card outlined class="card">
              <div class="d-flex justify-space-between">
                <div>
                  <v-card-title>Selected Tokens before change</v-card-title>
                  <v-card-subtitle
                    ><code>{{
                      selectedCommonTokens.raw
                    }}</code></v-card-subtitle
                  >
                </div>
                <v-card-actions
                  ><v-icon x-large @click="commonTokensSelected.before = false"
                    >mdi-close-circle-outline</v-icon
                  ></v-card-actions
                >
              </div>
              <v-divider />
              <v-virtual-scroll :item-height="20"></v-virtual-scroll>
              <common-tokens-item
                v-for="commonTokens in selectedCommonTokens.before"
                :key="
                  commonTokens.category.concat(
                    commonTokens.path,
                    commonTokens.range.toString()
                  )
                "
                :count="selectedCommonTokens.count"
                :common-tokens="commonTokens"
                :close-popup="() => (commonTokensSelected.before = false)"
              >
              </common-tokens-item>
            </v-card>
          </div>
        </div>
        <div v-show="commonTokensSelected.after" class="common-token-popup">
          <div class="inner">
            <v-card outlined class="card">
              <div class="d-flex justify-space-between">
                <div>
                  <v-card-title>Selected Tokens after change</v-card-title>
                  <v-card-subtitle
                    ><code>{{
                      selectedCommonTokens.raw
                    }}</code></v-card-subtitle
                  >
                </div>
                <v-card-actions
                  ><v-icon x-large @click="commonTokensSelected.after = false"
                    >mdi-close-circle-outline</v-icon
                  ></v-card-actions
                >
              </div>
              <v-divider />
              <common-tokens-item
                v-for="commonTokens in selectedCommonTokens.after"
                :key="
                  commonTokens.category.concat(
                    commonTokens.path,
                    commonTokens.range.toString()
                  )
                "
                :count="selectedCommonTokens.count"
                :common-tokens="commonTokens"
                :close-popup="() => (commonTokensSelected.after = false)"
              >
              </common-tokens-item>
            </v-card>
          </div>
        </div>
      </div>
      <div class="lock-editor-wrapper">
        <div v-show="lock.before" class="lock-editor">
          <div class="inner pa-2">
            <v-card outlined class="wh-100">
              <monaco-editor ref="beforeEditorRef" class="element-editor" />
            </v-card>
          </div>
        </div>
        <div v-show="lock.after" class="lock-editor">
          <div class="inner x-50 pa-2">
            <v-card outlined class="wh-100">
              <monaco-editor ref="afterEditorRef" class="element-editor" />
            </v-card>
          </div>
        </div>
      </div>
      <monaco-editor
        ref="editorRef"
        :is-loading="isLoading"
        class="element-editor"
      />
    </div>
  </div>
</template>

<script lang="ts">
import '@mdi/font/css/materialdesignicons.css'
import * as monaco from 'monaco-editor'
import {
  defineComponent,
  ref,
  watch,
  computed,
  onMounted,
  useContext,
  reactive,
} from '@nuxtjs/composition-api'
import { DiffCategory, FileMetadata } from 'refactorhub'
import MonacoEditor from '@/components/common/editor/MonacoEditor.vue'
import { useCodeFragmentDiff } from '@/components/draft/CodeFragmentDiff/CodeFragmentDiff.vue'
import { logger } from '@/utils/logger'
import {
  setupDisplayedFileOnDiffEditor,
  setupElementDecorationsOnDiffEditor,
  setupElementWidgetsOnDiffEditor,
  setupCommonTokensDecorationsOnBothDiffEditor,
  clearCommonTokensDecorationsOnBothDiffEditor,
} from './ts/displayedFile'
import { setupEditingElement } from './ts/editingElement'
import {
  CommonTokens,
  getCommonTokensSetOf,
} from './ts/commonTokensDecorations'

export default defineComponent({
  setup() {
    const {
      app: { $accessor },
    } = useContext()

    const editorRef = ref<InstanceType<typeof MonacoEditor>>()
    const beforeEditorRef = ref<InstanceType<typeof MonacoEditor>>()
    const afterEditorRef = ref<InstanceType<typeof MonacoEditor>>()
    const pending = ref(0)
    const isLoading = computed(() => pending.value > 0)
    const lock = reactive({
      before: false,
      after: false,
    })
    const lockFile = reactive<
      {
        [category in DiffCategory]?: FileMetadata
      }
    >({
      before: undefined,
      after: undefined,
    })

    const commonTokensSelected = reactive({
      before: false,
      after: false,
    })
    const selectedCommonTokens: {
      raw: string
      count: number
      before: CommonTokens[]
      after: CommonTokens[]
    } = reactive({
      raw: '',
      count: 0,
      before: [],
      after: [],
    })
    const showCommonTokens = reactive({ value: false })

    async function onChangeDisplayedFileMetadata(
      category: DiffCategory,
      metadata?: FileMetadata
    ) {
      console.log('onChangeDisplayedFileMetadata')
      if (!metadata) return
      const diffEditor = editorRef.value?.diffEditor
      if (!diffEditor) {
        logger.log('diffEditor is not loaded')
        return
      }

      pending.value++
      await setupDisplayedFileOnDiffEditor(
        category,
        metadata,
        diffEditor,
        $accessor,
        showCommonTokens.value,
        !lock[category]
      )
      pending.value--
    }

    watch(
      () => $accessor.draft.displayedFile.before,
      (value) => onChangeDisplayedFileMetadata('before', value)
    )
    watch(
      () => $accessor.draft.displayedFile.after,
      (value) => onChangeDisplayedFileMetadata('after', value)
    )

    watch(
      () => $accessor.draft.editingElement.before,
      (value) => setupEditingElement('before', value)
    )
    watch(
      () => $accessor.draft.editingElement.after,
      (value) => setupEditingElement('after', value)
    )

    watch(
      () => $accessor.draft.draft,
      () => {
        const diffEditor = editorRef.value?.diffEditor
        if (!diffEditor) {
          logger.log('diffEditor is not loaded')
          return
        }
        const before = $accessor.draft.displayedFile.before
        if (before) {
          if (lock.before) {
            const file = lockFile.before
            const editor = beforeEditorRef.value?.diffEditor
            if (file && editor) {
              setupElementDecorationsOnDiffEditor(
                'before',
                file,
                editor,
                $accessor
              )
            }
          } else {
            setupElementDecorationsOnDiffEditor(
              'before',
              before,
              diffEditor,
              $accessor
            )
          }
        }
        const after = $accessor.draft.displayedFile.after
        if (after) {
          if (lock.after) {
            const file = lockFile.after
            const editor = afterEditorRef.value?.diffEditor
            if (file && editor) {
              setupElementDecorationsOnDiffEditor(
                'after',
                file,
                editor,
                $accessor
              )
            }
          } else {
            setupElementDecorationsOnDiffEditor(
              'after',
              after,
              diffEditor,
              $accessor
            )
          }
        }
      }
    )

    watch(
      () => showCommonTokens.value,
      (value) => {
        if (value) {
          const diffEditor = editorRef.value?.diffEditor
          if (!diffEditor) {
            logger.log('diffEditor is not loaded')
            return
          }
          setupCommonTokensDecorationsOnBothDiffEditor(diffEditor, $accessor)
        } else {
          clearCommonTokensDecorationsOnBothDiffEditor()
        }
      }
    )

    onMounted(() => {
      const { open, setContents } = useCodeFragmentDiff()
      const diffEditor = editorRef.value?.diffEditor
      if (!diffEditor) {
        logger.log('diffEditor is not loaded')
        return
      }
      const getContent = (category: DiffCategory) => {
        const editor =
          category === 'before'
            ? diffEditor.getOriginalEditor()
            : diffEditor.getModifiedEditor()
        const selection = editor.getSelection()
        return selection
          ? editor.getModel()?.getValueInRange(selection) || ''
          : ''
      }

      function onMouseDown(e: monaco.editor.IEditorMouseEvent) {
        console.log(e)
        if (e.target.type === monaco.editor.MouseTargetType.CONTENT_WIDGET) {
          const content = e.target.element?.textContent ?? ''
          if (content.startsWith('View Common Tokens: ')) {
            const commonTokensRaw = content.substring(
              'View Common Tokens: '.length
            )
            const commonTokensSet = getCommonTokensSetOf(commonTokensRaw)

            selectedCommonTokens.raw = commonTokensRaw
            selectedCommonTokens.count = commonTokensSet.size
            selectedCommonTokens.before = [...commonTokensSet].filter(
              (c) => c.category === 'before'
            )
            selectedCommonTokens.after = [...commonTokensSet].filter(
              (c) => c.category === 'after'
            )
            commonTokensSelected.before = true
            commonTokensSelected.after = true
          }
        }
      }
      diffEditor.getOriginalEditor().onMouseDown((e) => onMouseDown(e))
      diffEditor.getModifiedEditor().onMouseDown((e) => onMouseDown(e))
      function onMouseMove(
        e: monaco.editor.IEditorMouseEvent,
        diffCategory: DiffCategory
      ) {
        if (e.target.type !== monaco.editor.MouseTargetType.CONTENT_TEXT) return
        if (!showCommonTokens.value || !diffEditor) return
        setupCommonTokensDecorationsOnBothDiffEditor(
          diffEditor,
          $accessor,
          e.target.position ? [diffCategory, e.target.position] : undefined
        )
      }
      diffEditor
        .getOriginalEditor()
        .onMouseMove((e) => onMouseMove(e, 'before'))
      diffEditor.getModifiedEditor().onMouseMove((e) => onMouseMove(e, 'after'))

      diffEditor.addAction({
        id: 'compare_selections',
        label: 'Compare Selections',
        contextMenuGroupId: '9_cutcopypaste',
        run() {
          setContents({
            before: getContent('before'),
            after: getContent('after'),
          })
          open()
        },
      })
    })

    async function lockEditor(category: DiffCategory) {
      if (!lock[category]) {
        const diffEditor = editorRef.value?.diffEditor
        if (!diffEditor) {
          logger.log('diffEditor is not loaded')
          return
        }

        const target = category === 'before' ? beforeEditorRef : afterEditorRef
        target.value?.diffEditor?.setModel({
          original:
            diffEditor.getOriginalEditor().getModel() ||
            monaco.editor.createModel('', 'text/plain'),
          modified:
            diffEditor.getModifiedEditor().getModel() ||
            monaco.editor.createModel('', 'text/plain'),
        })
        const state = diffEditor.saveViewState()
        if (state) target.value?.diffEditor?.restoreViewState(state)
        const metadata = $accessor.draft.displayedFile[category]
        const editor = target.value?.diffEditor
        if (metadata && editor) {
          lockFile[category] = metadata
          await setupElementDecorationsOnDiffEditor(
            category,
            metadata,
            editor,
            $accessor
          )
          await setupElementWidgetsOnDiffEditor(
            category,
            metadata,
            editor,
            $accessor
          )
          setupEditingElement(
            category,
            $accessor.draft.editingElement[category]
          )
        }

        lock[category] = true
      } else {
        const metadata = $accessor.draft.displayedFile[category]
        const diffEditor = editorRef.value?.diffEditor
        if (metadata && diffEditor) {
          await setupElementDecorationsOnDiffEditor(
            category,
            metadata,
            diffEditor,
            $accessor
          )
          await setupElementWidgetsOnDiffEditor(
            category,
            metadata,
            diffEditor,
            $accessor
          )
          setupEditingElement(
            category,
            $accessor.draft.editingElement[category]
          )
        }
        lock[category] = false
      }
    }

    return {
      editorRef,
      beforeEditorRef,
      afterEditorRef,
      isLoading,
      lockEditor,
      lock,
      commonTokensSelected,
      selectedCommonTokens,
      showCommonTokens,
    }
  },
})
</script>

<style lang="scss" scoped>
.position-relative {
  position: relative;
}
.wh-100 {
  width: 100%;
  height: 100%;
}

.common-token-popup-wrapper {
  position: absolute;
  width: 100%;
  height: 100%;
  z-index: 200;
  background: transparent;
  pointer-events: none;

  .common-token-popup {
    position: absolute;
    width: 50%;
    height: 100%;
    background: transparent;
    overflow-x: hidden;
    pointer-events: auto;

    &:nth-child(2) {
      left: 50%;
    }

    .inner {
      height: 100%;
      .card {
        background-color: #f0f0f0;
        height: max-content;
        min-height: 100%;
      }
    }

    .x-50 {
      transform: translateX(-50%);
    }
  }
}

.lock-editor-wrapper {
  position: absolute;
  width: 100%;
  height: 100%;
  z-index: 100;
  background: transparent;
  pointer-events: none;

  .lock-editor {
    position: absolute;
    width: 50%;
    height: 100%;
    background: transparent;
    overflow-x: hidden;
    pointer-events: auto;

    &:nth-child(2) {
      left: 50%;
    }

    .inner {
      min-width: 200%;
      height: 100%;
    }

    .x-50 {
      transform: translateX(-50%);
    }
  }
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
<style lang="scss">
*,
::before,
::after {
  background-repeat: repeat !important; // in order to display shaded area in diff editor
}
</style>
