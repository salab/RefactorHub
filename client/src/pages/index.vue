<template>
  <v-container>
    <div class="pa-5 text-center">
      <h1 class="display-3">RefactorHub</h1>
    </div>
    <v-divider />
    <v-row justify="center" class="pa-5">
      <div v-for="experiment in actives" :key="experiment.id" class="ma-3">
        <v-btn :to="`/experiment/${experiment.id}`" class="text-none">
          {{ experiment.title }}
        </v-btn>
      </div>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import {
  computed,
  defineComponent,
  ref,
  useAsync,
} from '@nuxtjs/composition-api'
import apis, { Experiment } from '@/apis'

export default defineComponent({
  setup() {
    const experiments = ref<Experiment[]>([])
    useAsync(async () => {
      experiments.value = (await apis.experiments.getAllExperiments()).data
    })

    const actives = computed(() => experiments.value.filter((e) => e.isActive))

    return { experiments, actives }
  },
})
</script>
