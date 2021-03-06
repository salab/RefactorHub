<template>
  <v-container v-if="experiment">
    <div class="py-3">
      <div class="d-flex align-center justify-space-between">
        <h1 class="text-h4">{{ experiment.title }}</h1>
        <v-btn
          depressed
          class="text-none"
          :href="`/api/experiments/${experiment.id}/result`"
        >
          Get All Result
        </v-btn>
      </div>
      <p class="py-1 mb-0 text-body-1">{{ experiment.description }}</p>
    </div>
    <v-divider />
    <div class="py-2">
      <div>
        <v-card
          v-for="(refactoring, i) in refactorings"
          :key="refactoring.id"
          outlined
          class="my-4"
        >
          <div class="d-flex align-center">
            <div class="pa-4">
              <v-icon v-if="isCompleted(refactoring.id)" color="success"
                >fa-check-circle</v-icon
              >
              <v-icon v-else>far fa-circle</v-icon>
            </div>
            <div class="flex-grow-1 py-2">
              <div class="title">
                {{ i + 1 }}. {{ refactoring.type }} (id={{ refactoring.id }})
              </div>
              <div>
                <div class="body-2">{{ refactoring.description }}</div>
              </div>
            </div>
            <div class="pa-4">
              <v-btn block depressed @click="() => start(refactoring.id)">
                start
              </v-btn>
            </div>
          </div>
        </v-card>
      </div>
    </div>
  </v-container>
</template>

<script lang="ts">
import {
  defineComponent,
  onMounted,
  ref,
  useAsync,
  useContext,
} from '@nuxtjs/composition-api'
import apis, { Experiment, Refactoring, RefactoringDraft } from '@/apis'

export default defineComponent({
  middleware: 'authenticated',
  setup() {
    const {
      params,
      app: { router },
    } = useContext()

    const experiment = ref<Experiment>()
    const refactorings = ref<Refactoring[]>([])
    const myRefactorings = ref<Refactoring[]>([])
    const myDrafts = ref<RefactoringDraft[]>([])

    useAsync(async () => {
      experiment.value = (
        await apis.experiments.getExperiment(parseInt(params.value.id))
      ).data
      refactorings.value = (
        await apis.experiments.getExperimentRefactorings(
          parseInt(params.value.id)
        )
      ).data
      const me = (await apis.users.getMe()).data
      myRefactorings.value = (await apis.users.getUserRefactorings(me.id)).data
      myDrafts.value = (await apis.users.getUserDrafts(me.id)).data
    })

    onMounted(async () => {
      const me = (await apis.users.getMe()).data
      myRefactorings.value = (await apis.users.getUserRefactorings(me.id)).data
      myDrafts.value = (await apis.users.getUserDrafts(me.id)).data
    })

    const isCompleted = (id: number) =>
      myRefactorings.value.some((ref) => ref.parentId === id)

    const start = async (id: number) => {
      const myRef = myRefactorings.value.find((ref) => ref.parentId === id)
      if (myRef) {
        // if already annotated
        const draft = (await apis.refactorings.editRefactoring(myRef.id)).data
        router?.push(`/draft/${draft.id}`)
        return
      }

      const myDraft = myDrafts.value.find((ref) => ref.originId === id)
      if (myDraft) {
        // if on annotating
        router?.push(`/draft/${myDraft.id}`)
        return
      }

      const draft = (await apis.refactorings.forkRefactoring(id)).data
      router?.push(`/draft/${draft.id}`)
    }

    return { experiment, refactorings, isCompleted, start }
  },
})
</script>
