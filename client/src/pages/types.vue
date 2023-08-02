<script setup lang="ts">
import apis, {
  CodeElementMetadata,
  CreateRefactoringTypeRequest,
  RefactoringType,
} from '@/apis'

const examples = [
  `{
  "name": "Extract Method",
  "description": "Extracting code fragments from existing method, and creating a new method based on the extracted code",
  "url": "https://refactoring.com/catalog/extractFunction.html",
  "before": {
    "extracted code": {
      "description": "Code fragments which is extracted",
      "type": "CodeFragment",
      "multiple": true,
      "required": true
    },
    "target method": {
      "description": "Method in which the extraction is performed",
      "type": "MethodDeclaration",
      "autofills": [
        {
          "type": "Surround",
          "follows": [
            {
              "category": "before",
              "name": "extracted code"
            }
          ],
          "element": "MethodDeclaration"
        }
      ]
    }
  },
  "after": {
    "invocation": {
      "description": "Method invocation by which extracted code was replaced",
      "type": "MethodInvocation",
      "required": true
    },
    "extracted code": {
      "description": "Code fragments which was extracted",
      "type": "CodeFragment",
      "multiple": true,
      "required": true
    },
    "extracted method": {
      "description": "Method which was newly created by the extraction",
      "type": "MethodDeclaration",
      "autofills": [
        {
          "type": "Surround",
          "follows": [
            {
              "category": "after",
              "name": "extracted code"
            }
          ],
          "element": "MethodDeclaration"
        }
      ]
    },
    "target method": {
      "description": "Method in which the extraction was performed",
      "type": "MethodDeclaration",
      "autofills": [
        {
          "type": "Surround",
          "follows": [
            {
              "category": "after",
              "name": "invocation"
            }
          ],
          "element": "MethodDeclaration"
        }
      ]
    }
  }
}`,
]

const types = ref<RefactoringType[]>([])
const form = reactive({
  type: '',
})
const message = ref('')
const pending = ref(false)

useAsyncData(async () => {
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

const create = async () => {
  if (form.type === '') return
  pending.value = true
  try {
    const request: CreateRefactoringTypeRequest = JSON.parse(form.type.trim())
    const type = (await apis.refactoringTypes.createRefactoringType(request))
      .data
    types.value.push(type)
    form.type = ''
    message.value = `RefactoringType added: ${type.name}`
  } catch (e) {
    message.value = `Failed: ${e}`
  } finally {
    pending.value = false
  }
}
</script>

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
          variant="outlined"
          style="border-color: lightgrey"
          class="my-4 px-4 py-3"
        >
          <div class="text-h5">{{ type.name }}</div>
          <div class="text-body-1">{{ type.description }}</div>
          <a :href="type.url">{{ type.url }}</a>
          <v-row no-gutters class="mt-2">
            <v-col class="mr-2">
              <v-card
                class="flex-grow-1 px-3 py-2"
                variant="outlined"
                style="border-color: lightgrey"
              >
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
              <v-card
                class="flex-grow-1 px-3 py-2"
                variant="outlined"
                style="border-color: lightgrey"
              >
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
    <v-divider />
    <div class="py-3">
      <h2 class="text-h5">Create a new RefactoringType</h2>
      <v-card
        variant="outlined"
        class="my-3 pa-4"
        style="border-color: lightgrey"
      >
        <v-textarea
          v-model="form.type"
          variant="underlined"
          label="refactoring type (JSON)"
          rows="3"
          hide-details
          class="mt-4 mb-2"
        />
        <details class="mb-4">
          <summary class="text-caption">examples for refactoring type</summary>
          <pre
            v-for="example in examples"
            :key="example"
            class="example px-3 py-2 mt-2"
          ><code>{{ example }}</code></pre>
        </details>
        <div>{{ message }}</div>
        <div class="d-flex justify-center">
          <v-btn :disabled="pending" depressed color="primary" @click="create">
            Create
          </v-btn>
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
