<template>
  <v-container>
    <div class="py-3">
      <h1>Experiment</h1>
      <v-divider />
    </div>
    <div class="py-2">
      <div>
        <v-card
          v-for="refactoring in refactorings"
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
                {{ refactoring.type }} (id={{ refactoring.id }})
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
import apis, { Refactoring, RefactoringDraft } from '@/apis'

export default defineComponent({
  name: 'experiment',
  middleware: 'authenticated',
  setup() {
    const {
      params,
      app: { router },
    } = useContext()

    const refactorings = ref<Refactoring[]>([])
    const myRefactorings = ref<Refactoring[]>([])
    const myDrafts = ref<RefactoringDraft[]>([])

    useAsync(async () => {
      refactorings.value = (
        await apis.users.getUserRefactorings(parseInt(params.value.id))
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

    return { refactorings, isCompleted, start }
  },
})
</script>
