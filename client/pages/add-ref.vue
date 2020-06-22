<template>
  <v-container>
    <h1>Add Refactoring</h1>
    <v-divider class="my-2" />
    <h2>with NDJSON</h2>
    <v-row>
      <v-col>
        <v-row no-gutters>
          <v-file-input label="input NDJSON file" @change="onInputFile" />
        </v-row>
        <v-row no-gutters>
          <template v-if="loading">
            <v-progress-linear indeterminate />
          </template>
          <template v-else>
            <div v-for="message in messages" :key="message">
              {{ message }}
            </div>
          </template>
        </v-row>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import { defineComponent, ref } from '@vue/composition-api'
import { Commit, RefactoringData } from 'refactorhub'
import { readAsText } from '@/utils/file'

export default defineComponent({
  name: 'add-ref',
  setup(_, { root }) {
    const messages = ref<string[]>([])
    const loading = ref(false)

    async function onInputFile(file: File) {
      loading.value = true
      const content = await readAsText(file)
      if (!content) {
        messages.value = ['Failed to load NDJSON file.']
        loading.value = false
        return
      }

      const refs: {
        type: string
        description: string
        commit: Commit
        data: RefactoringData
      }[] = content
        .trim()
        .split('\n')
        .map((it) => JSON.parse(it))

      try {
        const added = await Promise.all(
          refs.map((ref) =>
            root.$client.addRefactoring(
              ref.type,
              ref.description,
              ref.commit,
              ref.data
            )
          )
        )
        messages.value = ['Added refs:', ...added.map((ref) => ref.description)]
      } finally {
        loading.value = false
      }
    }

    return {
      messages,
      loading,
      onInputFile,
    }
  },
})
</script>
