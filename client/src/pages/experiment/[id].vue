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
  experiment.value = await useExperiment().get(experimentId)
})

onMounted(async () => {
  const me = (await apis.users.getMe()).data
  myAnnotations.value = (await apis.users.getUserAnnotationIds(me.id)).data
})

function getAnnotation(commitId: string) {
  return myAnnotations.value.find(
    (annotation) =>
      annotation.experimentId === experimentId &&
      annotation.commitId === commitId,
  )
}
const getStatus = (commitId: string): 'done' | 'draft' | 'notStarted' => {
  const annotation = getAnnotation(commitId)
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
    <div class="d-flex align-end pb-1">
      <h1 class="text-h4">{{ experiment.title }}</h1>
      <p class="text-body-1 ml-2">- {{ experiment.description }}</p>
      <v-spacer />
      <v-btn
        flat
        text="result"
        :href="`/api/experiments/${experiment.id}/result`"
      />
    </div>
    <v-divider />
    <v-breadcrumbs
      class="pa-0"
      style="font-size: small"
      :items="[
        { title: 'Experiments', disabled: false, href: '/experiment' },
        {
          title: experiment.title,
          disabled: true,
          href: `/experiment/${experimentId}`,
        },
      ]"
      ><template #divider>
        <v-icon size="small" icon="$mdiChevronRight"></v-icon> </template
    ></v-breadcrumbs>
    <div>
      <div>
        <v-card
          v-for="(commit, i) in experiment.targetCommits"
          :key="commit.id"
          variant="outlined"
          style="border-color: lightgrey"
          class="my-4"
        >
          <v-row no-gutters class="d-flex align-center">
            <v-col :cols="1" class="d-flex justify-center">
              <v-icon
                v-if="getStatus(commit.id) === 'done'"
                icon="$mdiCheckCircle"
                size="x-large"
                color="success"
              />
              <v-icon
                v-else-if="getStatus(commit.id) === 'draft'"
                icon="$mdiProgressCheck"
                size="x-large"
              />
            </v-col>
            <v-col :cols="9">
              <v-card-title class="px-0">
                {{ i + 1 }}. {{ commit.owner }}/{{ commit.repository }}/{{
                  commit.sha.substring(0, 7)
                }}
                <a :href="commit.url" target="_blank" rel="noopener"
                  ><v-icon icon="$mdiGithub" size="small" class="pb-1"
                /></a>
              </v-card-title>
              <v-card-text class="px-0 pb-1">
                {{ commit.message }}
              </v-card-text>
            </v-col>
            <v-col :cols="2" class="d-flex flex-column">
              <v-btn
                v-if="getStatus(commit.id) !== 'notStarted'"
                flat
                text="result"
                :href="`/api/annotations/${
                  getAnnotation(commit.id)?.annotationId
                }/data`"
                class="ma-2 mb-0"
              />
              <v-btn
                flat
                text="start"
                class="ma-2"
                @click="() => start(commit.id)"
              />
            </v-col>
          </v-row>
        </v-card>
      </div>
    </div>
  </v-container>
</template>
