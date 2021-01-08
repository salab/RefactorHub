<template>
  <div class="d-flex flex-column fill-height">
    <code-fragment-diff />
    <div class="d-flex">
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
      <v-divider vertical />
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
} from './ts/displayedFile'
import { setupEditingElement } from './ts/editingElement'

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

    async function onChangeDisplayedFileMetadata(
      category: DiffCategory,
      metadata?: FileMetadata
    ) {
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
}
</style>
