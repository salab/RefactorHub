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
        <v-row no-gutters>
          <template v-if="loading">
            <v-progress-linear indeterminate />
          </template>
          <template v-else>
            {{ message }}
          </template>
        </v-row>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import { defineComponent, ref } from '@nuxtjs/composition-api'
import { CodeElementMetadata } from 'refactorhub'
import { readAsText } from '@/utils/file'

export default defineComponent({
  name: 'add-type',
  setup(_, { root }) {
    const message = ref('')
    const loading = ref(false)

    async function onInputFile(file: File) {
      loading.value = true
      const content = await readAsText(file)
      if (!content) {
        message.value = 'Failed to load JSON file.'
        loading.value = false
        return
      }

      const types: {
        name: string
        before: { [key: string]: CodeElementMetadata }
        after: { [key: string]: CodeElementMetadata }
      }[] = JSON.parse(content)

      try {
        const added = await Promise.all(
          types.map((type) =>
            root.$client.createRefactoringType(
              type.name,
              type.before,
              type.after
            )
          )
        )
        message.value = `Added types: ${added
          .map((type) => type.name)
          .join(', ')}`
      } finally {
        loading.value = false
      }
    }

    return {
      message,
      loading,
      onInputFile,
    }
  },
})
</script>
