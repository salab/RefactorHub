<template>
  <v-container>
    <div class="pa-5 text-center">
      <h1 class="display-3">RefactorHub</h1>
    </div>
    <v-divider />
    <v-row v-if="!authenticated" justify="center" class="pa-5">
      <div class="ma-3">
        <v-btn href="/login" class="text-none">Login</v-btn>
      </div>
    </v-row>
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
    const authenticated = ref(true)
    const experiments = ref<Experiment[]>([])
    useAsync(async () => {
      try {
        await apis.users.getMe()
        experiments.value = (await apis.experiments.getAllExperiments()).data
      } catch {
        authenticated.value = false
      }
    })

    const actives = computed(() => experiments.value.filter((e) => e.isActive))

    return { authenticated, experiments, actives }
  },
})
</script>
