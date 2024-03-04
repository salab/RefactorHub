<script setup lang="ts">
import { ActionName, Experiment } from '@/apis'

const form = reactive({
  title: '',
  description: '',
  commits: '',
})
const message = ref('')
const pending = ref(false)
const examples = [
  [
    `{"sha":"cb49e436b9d7ee55f2531ebc2ef1863f5c9ba9fe","owner":"rstudio","repository":"rstudio"}`,
    `{"sha":"f05e86c4d31987ff2f30330745c3eb605de4c4dc","owner":"Graylog2","repository":"graylog2-server"}`,
    `{"sha":"08f37df9f39f101bba0ee96845e232d2c72bf426","owner":"JetBrains","repository":"intellij-community"}`,
  ].join('\n'),
  [
    `{"sha":"6cf596df183b3c3a38ed5dd9bb3b0100c6548ebb","owner":"realm","repository":"realm-java"}`,
    `{"sha":"6abc40ed4850d74ee6c155f5a28f8b34881a0284","owner":"BuildCraft","repository":"BuildCraft"}`,
  ].join('\n'),
]
const experiments = ref<Experiment[]>([])
useAsyncData(async () => {
  experiments.value = await useExperiment().getAll()
})

const create = async () => {
  if (form.title === '' || form.commits === '') return
  pending.value = true
  try {
    const commits: { owner: string; repository: string; sha: string }[] =
      form.commits
        .trim()
        .split('\n')
        .map((it) => JSON.parse(it))
    experiments.value = await useExperiment().create({
      title: form.title,
      description: form.description,
      commits,
    })
    message.value = `Experiment added: ${form.title}`
    form.title = ''
    form.description = ''
    form.commits = ''
  } catch (e) {
    message.value = `Failed: ${e}`
  } finally {
    pending.value = false
  }
}

sendAction(ActionName.OpenExperiments)

function sortExperiments(experiments: Experiment[]): Experiment[] {
  const array = [...experiments]
  // HARD CODING
  function getPriority(experiment: Experiment): number {
    if (experiment.title.startsWith('Demonstration')) return 0
    if (experiment.title.startsWith('Tutorial')) {
      if (experiment.title.startsWith('Tutorial Tool')) return 10
      if (experiment.title.startsWith('Tutorial Refactoring')) return 11
      if (experiment.title.startsWith('Tutorial Exercise')) return 12
    }
    if (experiment.title.startsWith('Experiment')) return 100
    return 1000
  }
  array.sort((a, b) => {
    const pa = getPriority(a)
    const pb = getPriority(b)
    if (pa !== pb) return pa - pb
    if (a.title > b.title) return 1
    if (a.title < b.title) return -1
    return 0
  })
  return array
}
</script>

<template>
  <v-container>
    <div class="d-flex align-end pb-1">
      <h1 class="text-h4">Experiments</h1>
    </div>
    <v-divider />
    <div class="py-2">
      <div v-if="experiments.length === 0">
        Loading Experiments... Please wait a moment
      </div>
      <div v-else>
        <v-card
          v-for="experiment in sortExperiments(
            experiments.filter((experiment) => experiment.isActive),
          )"
          :key="experiment.id"
          variant="outlined"
          style="border-color: lightgrey"
          :title="experiment.title"
          :subtitle="experiment.description"
          class="my-4"
          :to="`/experiment/${experiment.id}`"
        />
      </div>
    </div>
    <v-divider />
    <div class="py-3">
      <h2 class="text-h5">Create a new Experiment</h2>
      <v-card
        variant="outlined"
        style="border-color: lightgrey"
        class="my-3 pa-4"
      >
        <v-text-field
          v-model="form.title"
          variant="underlined"
          label="title"
          hide-details
          class="mb-4"
        />
        <v-textarea
          v-model="form.description"
          variant="underlined"
          label="description"
          rows="1"
          hide-details
          class="my-4"
        />
        <v-textarea
          v-model="form.commits"
          variant="underlined"
          label="commits (NDJSON)"
          rows="3"
          hide-details
          class="mt-4 mb-2"
        />
        <details class="mb-4">
          <summary class="text-caption">examples for commits</summary>
          <pre
            v-for="example in examples"
            :key="example"
            class="example px-3 py-2 mt-2"
          ><code>{{ example }}</code></pre>
        </details>
        <div>{{ message }}</div>
        <div class="d-flex justify-center">
          <v-btn :disabled="pending" flat @click="create"> Create </v-btn>
        </div>
      </v-card>
    </div>
  </v-container>
</template>

<style lang="scss" scoped>
.example {
  overflow-x: auto;
  background: whitesmoke;
  code {
    padding: 0;
    background: transparent;
  }
}
</style>
