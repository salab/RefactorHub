<template>
  <v-container>
    <h1>Add Refactoring type</h1>
    <v-divider class="my-2" />
    <h2>with JSON</h2>
    <v-row>
      <v-col>
        <v-row no-gutters>
          <v-file-input label="input JSON file" @change="onInputFile" />
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
import apis, { CodeElementMetadata } from '@/apis'
import { readAsText } from '@/utils/file'

export default defineComponent({
  middleware: 'authenticated',
  setup() {
    const messages = ref<string[]>([])
    const loading = ref(false)

    async function onInputFile(file: File) {
      loading.value = true
      const content = await readAsText(file)
      if (!content) {
        messages.value = ['Failed to load JSON file.']
        loading.value = false
        return
      }

      const types: {
        name: string
        before: { [key: string]: CodeElementMetadata }
        after: { [key: string]: CodeElementMetadata }
      }[] = JSON.parse(content)

      try {
        const addedTypes = await Promise.all(
          types.map(
            async (type) =>
              (
                await apis.refactoringTypes.createRefactoringType({
                  name: type.name,
                  before: type.before,
                  after: type.after,
                  description: '',
                })
              ).data
          )
        )
        messages.value = [
          'Added RefactoringTypes:',
          ...addedTypes.map((type) => type.name),
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
