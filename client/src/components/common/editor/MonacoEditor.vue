<script setup lang="ts">
import * as monaco from 'monaco-editor'
import cryptoRandomString from 'crypto-random-string'

const props = defineProps({
  before: {
    type: Object as () => monaco.editor.ITextModel,
    default: undefined,
  },
  after: {
    type: Object as () => monaco.editor.ITextModel,
    default: undefined,
  },
})
const id = cryptoRandomString({ length: 10 })

const lineCount = ref(0)

onMounted(() => {
  const container = document.getElementById(id)
  if (container === null) return
  if (props.before && props.after) {
    const diffEditor = monaco.editor.createDiffEditor(container, {
      automaticLayout: true,
      readOnly: true,
    })
    diffEditor.setModel({
      original: props.before,
      modified: props.after,
    })
    lineCount.value = Math.max(
      props.before.getLineCount(),
      props.after.getLineCount(),
    )
    return
  }
  const model = props.before ?? props.after
  if (!model) return
  const editor = monaco.editor.create(container, {
    automaticLayout: true,
    readOnly: true,
  })
  editor.setModel(model)
  lineCount.value = model.getLineCount()
})
</script>

<template>
  <div
    :id="id"
    :style="`width: 100%; height: ${lineCount < 10 ? lineCount * 20 : 200}px`"
  />
</template>
