<template>
  <div class="editor-wrapper">
    <code-fragment-diff />
    <monaco-editor
      ref="editorRef"
      :is-loading="isLoading"
      class="element-editor"
    />
  </div>
</template>

<script lang="ts">
import {
  defineComponent,
  ref,
  watch,
  computed,
  onMounted,
  useContext,
} from '@nuxtjs/composition-api'
import { DiffCategory, FileMetadata } from 'refactorhub'
import MonacoEditor from '@/components/common/editor/MonacoEditor.vue'
import CodeFragmentDiff, {
  useCodeFragmentDiff,
} from '@/components/draft/CodeFragmentDiff/CodeFragmentDiff.vue'
import { logger } from '@/utils/logger'
import { setupDisplayedFileOnDiffEditor } from './use/displayedFile'
import { setupEditingElement } from './use/editingElement'

export default defineComponent({
  name: 'ElementEditor',
  components: {
    MonacoEditor,
    CodeFragmentDiff,
  },
  setup() {
    const {
      app: { $accessor },
    } = useContext()

    const editorRef = ref<InstanceType<typeof MonacoEditor>>()
    const pending = ref(0)
    const isLoading = computed(() => pending.value > 0)

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
        $accessor
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
      () => $accessor.draft.editingElementMetadata.before,
      (value) => setupEditingElement('before', value)
    )
    watch(
      () => $accessor.draft.editingElementMetadata.after,
      (value) => setupEditingElement('after', value)
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

    return {
      editorRef,
      isLoading,
    }
  },
})
</script>

<style lang="scss" scoped>
.editor-wrapper {
  width: 100%;
  height: 100%;
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
