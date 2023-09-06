<script setup lang="ts">
import * as monaco from 'monaco-editor'
import { DiffCategory } from 'refactorhub'
import cryptoRandomString from 'crypto-random-string'
import { Range } from 'apis'
import { asMonacoRange } from '@/components/common/editor/utils/range'

function createAnnotatedElementLocationMap(
  category: DiffCategory,
): Map<string, { path: string; range: Range }> {
  const type = useDraft().refactoringTypes.value.find(
    (t) => t.name === useDraft().draft.value?.type,
  )
  const elementMetadataMap = type ? type[category] : {}

  const draft = useDraft().draft.value
  if (!draft) return new Map()
  const entries = Object.entries(draft.data[category])
  const map = elementMetadataMap
  entries.sort((a, b) => {
    if (a[0] in map && b[0] in map) {
      if (map[a[0]].required && !map[b[0]].required) return -1
      if (!map[a[0]].required && map[b[0]].required) return 1
    } else if (a[0] in map) return -1
    else if (b[0] in map) return 1
    return a[0] < b[0] ? -1 : a[0] > b[0] ? 1 : 0
  })
  const elementHolderMap = Object.fromEntries(entries)

  const annotatedElementLocationMap = new Map<
    string,
    { path: string; range: Range }
  >()
  for (const [elementKey, elementHolder] of Object.entries(elementHolderMap)) {
    for (let i = 0; i < elementHolder.elements.length; i++) {
      const element = elementHolder.elements[i]
      const path = element.location?.path
      const range = element.location?.range
      if (!path || !range) continue
      const elementName =
        elementHolder.elements.length === 1
          ? elementKey
          : `${elementKey} - ${i + 1}`
      annotatedElementLocationMap.set(elementName, { path, range })
    }
  }
  return annotatedElementLocationMap
}
const annotatedElementLocationMap = {
  before: computed(() => createAnnotatedElementLocationMap('before')),
  after: computed(() => createAnnotatedElementLocationMap('after')),
}

const selectedElementNameBefore = ref('')
const selectedElementNameAfter = ref('')
watch(
  () => [...annotatedElementLocationMap.before.value.keys()],
  (newKeys) => {
    if (!newKeys.includes(selectedElementNameBefore.value))
      selectedElementNameBefore.value = ''
  },
)
watch(
  () => [...annotatedElementLocationMap.after.value.keys()],
  (newKeys) => {
    if (!newKeys.includes(selectedElementNameAfter.value))
      selectedElementNameAfter.value = ''
  },
)

const diffViewerId = cryptoRandomString({ length: 10 })
let diffViewer: monaco.editor.IStandaloneDiffEditor | undefined
onMounted(() => {
  const container = document.getElementById(diffViewerId)
  if (!container) {
    logger.error(
      `Cannot find the container element to compare elements: id is ${diffViewerId}`,
    )
    return
  }
  diffViewer = monaco.editor.createDiffEditor(container, {
    enableSplitViewResizing: true,
    automaticLayout: true,
    readOnly: true,
    scrollBeyondLastLine: false,
  })
})
async function updateDiffViewer() {
  if (!diffViewer) {
    logger.error('diffViewer to compare elements is not created')
    return
  }
  const locationBefore = annotatedElementLocationMap.before.value.get(
    selectedElementNameBefore.value,
  )
  const locationAfter = annotatedElementLocationMap.after.value.get(
    selectedElementNameAfter.value,
  )
  const fileModelBefore = await useDraft().getTextModelOfFile(
    'before',
    locationBefore ? locationBefore.path : '',
  )
  const fileModelAfter = await useDraft().getTextModelOfFile(
    'after',
    locationAfter ? locationAfter.path : '',
  )
  diffViewer.setModel({
    original: monaco.editor.createModel(
      fileModelBefore.getValueInRange(asMonacoRange(locationBefore?.range)),
      fileModelBefore.getLanguageId(),
    ),
    modified: monaco.editor.createModel(
      fileModelAfter.getValueInRange(asMonacoRange(locationAfter?.range)),
      fileModelAfter.getLanguageId(),
    ),
  })
}
watch(
  () => selectedElementNameBefore.value,
  () => updateDiffViewer(),
)
watch(
  () => selectedElementNameAfter.value,
  () => updateDiffViewer(),
)
</script>

<template>
  <v-sheet>
    <v-row class="pa-3 pb-0">
      <v-col>
        <v-select
          v-model="selectedElementNameBefore"
          variant="underlined"
          clearable
          :items="[...annotatedElementLocationMap.before.value.keys()]"
          label="Annotated Before Element"
          hide-details
        />
      </v-col>
      <v-col>
        <v-select
          v-model="selectedElementNameAfter"
          variant="underlined"
          clearable
          :items="[...annotatedElementLocationMap.after.value.keys()]"
          label="Annotated After Element"
          hide-details
        />
      </v-col>
    </v-row>
    <div
      :id="diffViewerId"
      class="pa-3 pt-1"
      style="width: 100%; height: 300px"
    />
  </v-sheet>
</template>
