<template>
  <v-container fill-height>
    <v-col>
      <v-row>
        <div class="headline">Experiments</div>
      </v-row>
      <v-row>
        <v-col v-for="experiment in experiments" :key="experiment.id" cols="12">
          <v-row>
            <v-divider class="mb-4" />
          </v-row>
          <v-row align="center">
            <div class="title">
              {{ experiment.type.name }}(id={{ experiment.id }})
            </div>
            <v-btn class="text-none ml-2" @click="fork(experiment.id)">
              <v-icon left>fa-code-branch</v-icon>
              Fork
            </v-btn>
          </v-row>
          <v-row>
            <div class="subtitle-1">My Refactorings</div>
          </v-row>
          <v-row>
            <div
              v-for="refactoring in getRefactorings(experiment.id)"
              :key="refactoring.id"
              class="ma-2"
            >
              <v-btn class="text-none" @click="edit(refactoring.id)">
                {{ refactoring.type.name }}(id={{ refactoring.id
                }}{{
                  refactoring.parent ? `, parent=${refactoring.parent.id}` : ''
                }})
              </v-btn>
            </div>
          </v-row>
          <v-row>
            <div class="subtitle-1">My Drafts</div>
          </v-row>
          <v-row>
            <div
              v-for="draft in getDrafts(experiment.id)"
              :key="draft.id"
              class="ma-2"
            >
              <v-btn class="text-none" @click="open(draft.id)">
                {{ draft.type.name }}(id={{ draft.id
                }}{{ draft.parent ? `, parent=${draft.parent.id}` : ''
                }}{{ draft.origin ? `, origin=${draft.origin.id}` : '' }})
              </v-btn>
            </div>
          </v-row>
        </v-col>
      </v-row>
    </v-col>
  </v-container>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted } from '@vue/composition-api'
import { Refactoring, Draft, User } from 'refactorhub'

export default defineComponent({
  name: 'tutorial',
  setup(_, { root }) {
    const experiments = ref<Refactoring[]>([])
    const refactorings = ref<Refactoring[]>([])
    const drafts = ref<Draft[]>([])

    onMounted(async () => {
      const user = root.$auth.user as User
      experiments.value = await root.$client.getUserRefactorings(2)
      refactorings.value = await root.$client.getUserRefactorings(user.id)
      drafts.value = await root.$client.getUserDrafts(user.id)
    })

    async function fork(id: number) {
      const draft = await root.$client.forkRefactoring(id)
      root.$router.push(`/draft/${draft.id}`)
    }

    async function edit(id: number) {
      const draft = await root.$client.editRefactoring(id)
      root.$router.push(`/draft/${draft.id}`)
    }

    function open(id: number) {
      root.$router.push(`/draft/${id}`)
    }

    function getRefactorings(id: number) {
      return refactorings.value.filter((it) => it.parent?.id === id)
    }

    function getDrafts(id: number) {
      return drafts.value.filter((it) => it.parent?.id === id)
    }

    return {
      experiments,
      fork,
      edit,
      open,
      getRefactorings,
      getDrafts,
    }
  },
})
</script>
