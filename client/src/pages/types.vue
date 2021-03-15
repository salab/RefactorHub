<template>
  <v-container>
    <div class="py-3">
      <h1 class="text-h4">Refactoring Types</h1>
    </div>
    <v-divider />
    <div class="py-2">
      <div>
        <v-card
          v-for="type in types"
          :key="type.id"
          outlined
          class="my-4 px-4 py-3"
        >
          <div class="text-h5">{{ type.name }}</div>
          <div class="text-body-1">{{ type.description }}</div>
          <a :href="type.url">{{ type.url }}</a>
          <v-row no-gutters class="mt-2">
            <v-col class="mr-2">
              <v-card class="flex-grow-1 px-3 py-2" outlined>
                <div
                  v-for="(v, k) in sorted(type.before)"
                  :key="k"
                  class="mb-1"
                >
                  <div class="text-h6 d-flex align-center">
                    {{ v.required ? '*' : '' }}{{ k }}
                    <span class="text-subtitle-1 pl-1">({{ v.type }})</span>
                  </div>
                  <div class="text-body-1">{{ v.description }}</div>
                </div>
              </v-card>
            </v-col>
            <v-col class="ml-2">
              <v-card class="flex-grow-1 px-3 py-2" outlined>
                <div v-for="(v, k) in sorted(type.after)" :key="k" class="mb-1">
                  <div class="text-h6 d-flex align-center">
                    {{ v.required ? '*' : '' }}{{ k }}
                    <span class="text-subtitle-1 pl-1">({{ v.type }})</span>
                  </div>
                  <div class="text-body-1">{{ v.description }}</div>
                </div>
              </v-card>
            </v-col>
          </v-row>
        </v-card>
      </div>
    </div>
  </v-container>
</template>

<script lang="ts">
import { defineComponent, ref, useAsync } from '@nuxtjs/composition-api'
import apis, { CodeElementMetadata, RefactoringType } from '@/apis'

export default defineComponent({
  setup() {
    const types = ref<RefactoringType[]>([])

    useAsync(async () => {
      types.value = (await apis.refactoringTypes.getAllRefactoringTypes()).data
    })

    const sorted = (map: { [k: string]: CodeElementMetadata }) => {
      const entries = Object.entries(map)
      entries.sort((a, b) => {
        if (a[1].required && !b[1].required) return -1
        if (!a[1].required && b[1].required) return 1
        return a[0] < b[0] ? -1 : a[0] > b[0] ? 1 : 0
      })
      return Object.fromEntries(entries)
    }
    return { types, sorted }
  },
})
</script>
