<script setup lang="ts">
import apis, { Experiment, Refactoring, RefactoringDraft } from '@/apis'

definePageMeta({
  middleware: 'authenticated',
})

const paramId = useRoute().params.id
const experimentId =
  typeof paramId === 'string' ? parseInt(paramId) : parseInt(paramId[0])

const experiment = ref<Experiment>()
const refactorings = ref<Refactoring[]>([])
const myRefactorings = ref<Refactoring[]>([])
const myDrafts = ref<RefactoringDraft[]>([])

useAsyncData(async () => {
  experiment.value = (await apis.experiments.getExperiment(experimentId)).data
  refactorings.value = (
    await apis.experiments.getExperimentRefactorings(experimentId)
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
    navigateTo(`/draft/${draft.id}`)
    return
  }

  const myDraft = myDrafts.value.find((ref) => ref.originId === id)
  if (myDraft) {
    // if on annotating
    navigateTo(`/draft/${myDraft.id}`)
    return
  }

  const draft = (await apis.refactorings.forkRefactoring(id)).data
  navigateTo(`/draft/${draft.id}`)
}
</script>

<template>
  <v-container v-if="experiment">
    <div class="py-3">
      <div class="d-flex align-center justify-space-between">
        <h1 class="text-h4">{{ experiment.title }}</h1>
        <v-btn
          flat
          color="grey-lighten-4"
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
          variant="outlined"
          style="border-color: lightgrey"
          class="my-4"
        >
          <div class="d-flex align-center">
            <div class="px-4">
              <v-icon
                v-if="isCompleted(refactoring.id)"
                icon="$mdiCheckCircle"
                size="x-large"
                color="success"
              />
              <v-icon v-else icon="$mdiCircleOutline" size="x-large" />
            </div>
            <div class="flex-grow-1">
              <v-card-title>
                {{ i + 1 }}. {{ refactoring.type }} (id={{ refactoring.id }})
              </v-card-title>
              <v-card-text v-if="refactoring.description" class="pb-1">
                {{ refactoring.description }}
              </v-card-text>
            </div>
            <v-btn
              color="grey-lighten-4"
              flat
              class="text-none mx-2"
              @click="() => start(refactoring.id)"
            >
              START
            </v-btn>
          </div>
        </v-card>
      </div>
    </div>
  </v-container>
</template>
