<script setup lang="ts">
import apis, { Experiment } from '@/apis'

definePageMeta({
  middleware: 'authenticated',
})

const paramId = useRoute().params.id
const experimentId = typeof paramId === 'string' ? paramId : paramId[0]

const experiment = ref<Experiment>()
const myAnnotations = ref<
  {
    experimentId: string
    commitId: string
    annotationId: string
    isDraft: boolean
  }[]
>([])

useAsyncData(async () => {
  experiment.value =
    useExperiment().experimentMap.value.get(experimentId) ??
    (await apis.experiments.getExperiment(experimentId)).data
  useExperiment().experimentMap.value.set(experimentId, experiment.value)
  const me = (await apis.users.getMe()).data
  myAnnotations.value = (await apis.users.getUserAnnotationIds(me.id)).data
})

onMounted(async () => {
  const me = (await apis.users.getMe()).data
  myAnnotations.value = (await apis.users.getUserAnnotationIds(me.id)).data
})

const getStatus = (commitId: string): 'done' | 'draft' | 'notStarted' => {
  const annotation = myAnnotations.value.find(
    (annotation) =>
      annotation.experimentId === experimentId &&
      annotation.commitId === commitId,
  )
  if (!annotation) return 'notStarted'
  if (annotation.isDraft) return 'draft'
  return 'done'
}

const start = async (commitId: string) => {
  const annotationId = (
    await apis.experiments.startAnnotation(experimentId, commitId)
  ).data
  navigateTo(`/annotation/${annotationId}`)
}
</script>

<template>
  <v-container v-if="experiment">
    <div class="py-3">
      <div class="d-flex align-center justify-space-between">
        <h1 class="text-h4">{{ experiment.title }}</h1>
        <v-btn
          flat
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
          v-for="(commit, i) in experiment.targetCommits"
          :key="commit.id"
          variant="outlined"
          style="border-color: lightgrey"
          class="my-4"
        >
          <div class="d-flex align-center">
            <div class="px-4">
              <v-icon
                v-if="getStatus(commit.id) == 'done'"
                icon="$mdiCheckCircle"
                size="x-large"
                color="success"
              />
              <v-icon
                v-else-if="getStatus(commit.id) == 'draft'"
                icon="$mdiProgressCheck"
                size="x-large"
              />
              <v-icon v-else icon="$mdiCircleOutline" size="x-large" />
            </div>
            <div class="flex-grow-1">
              <v-card-title>
                {{ i + 1 }}. {{ commit.owner }}/{{ commit.repository }}/{{
                  commit.sha
                }}
              </v-card-title>
              <v-card-text class="pb-1">
                {{ commit.message }}
              </v-card-text>
            </div>
            <v-btn flat class="text-none mx-2" @click="() => start(commit.id)">
              START
            </v-btn>
          </div>
        </v-card>
      </div>
    </div>
  </v-container>
</template>
