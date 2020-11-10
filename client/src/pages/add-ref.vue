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
        <v-row v-if="loading" no-gutters>
          <v-progress-linear indeterminate />
        </v-row>
        <v-row v-for="message in messages" v-else :key="message" no-gutters>
          {{ message }}
        </v-row>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import { defineComponent, ref } from '@nuxtjs/composition-api'
import apis, { Commit, RefactoringData } from '@/apis'
import { readAsText } from '@/utils/file'

export default defineComponent({
  name: 'add-ref',
  middleware: 'authenticated',
  setup() {
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
        const addedRefs = await Promise.all(
          refs.map(
            async (ref) =>
              (
                await apis.refactorings.createRefactoring({
                  type: ref.type,
                  description: ref.description,
                  commit: ref.commit,
                  data: ref.data,
                })
              ).data
          )
        )
        messages.value = [
          'Added Refactorings:',
          ...addedRefs.map((ref) => ref.description),
        ]
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
