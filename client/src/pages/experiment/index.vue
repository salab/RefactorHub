<template>
  <v-container>
    <div class="py-3">
      <div class="d-flex align-center justify-space-between">
        <h1 class="text-h4">Experiments</h1>
        <v-btn
          depressed
          small
          class="text-none"
          :disabled="state === 'Pending'"
          @click="prepare"
        >
          {{ state }}
        </v-btn>
      </div>
    </div>
    <v-divider />
    <div class="py-2">
      <div>
        <v-card
          v-for="experiment in actives"
          :key="experiment.id"
          outlined
          class="my-4"
          :to="`/experiment/${experiment.id}`"
        >
          <div class="d-flex align-center">
            <div class="flex-grow-1 px-4 py-2">
              <div class="title">
                {{ experiment.title }}
              </div>
              <div>
                <div class="body-2">{{ experiment.description }}</div>
              </div>
            </div>
          </div>
        </v-card>
      </div>
    </div>
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
      try {
        experiments.value = (await apis.experiments.getAllExperiments()).data
      } catch {}
    })

    const actives = computed(() => experiments.value.filter((e) => e.isActive))

    const state = ref<'Prepare' | 'Pending' | 'Completed'>('Prepare')
    const prepare = async () => {
      state.value = 'Pending'
      const res = await apis.annotator.prepareCommitContents()
      if (res) state.value = 'Completed'
    }

    return { experiments, actives, state, prepare }
  },
})
</script>
