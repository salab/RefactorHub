<template>
  <v-container>
    <div class="py-10 text-center">
      <v-img src="/logo.png" contain max-height="100"></v-img>
      <div class="mt-6 text-h5">A Commit Annotator for Refactoring</div>
    </div>
    <template v-if="isAuthenticated">
      <v-divider />
      <div class="d-flex justify-center pa-6">
        <v-btn to="/experiment" depressed class="text-none mx-3">
          Experiments
        </v-btn>
        <v-btn to="/types" depressed class="text-none mx-3">
          Refactoring Types
        </v-btn>
      </div>
    </template>
    <v-divider />
    <div class="py-8 text-center">
      <h2 class="text-h4">Demonstration</h2>
      <iframe
        class="mt-4"
        width="560"
        height="315"
        src="https://www.youtube.com/embed/Ew1wVBZkpro"
        frameborder="0"
        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
        allowfullscreen
      ></iframe>
    </div>
  </v-container>
</template>

<script lang="ts">
import {
  computed,
  defineComponent,
  ref,
  useAsync,
  useContext,
} from '@nuxtjs/composition-api'
import apis, { Experiment } from '@/apis'

export default defineComponent({
  setup() {
    const {
      app: { $accessor },
    } = useContext()
    const isAuthenticated = computed(() => $accessor.isAuthenticated)

    const experiments = ref<Experiment[]>([])
    useAsync(async () => {
      try {
        experiments.value = (await apis.experiments.getAllExperiments()).data
      } catch {}
    })

    const actives = computed(() => experiments.value.filter((e) => e.isActive))

    return { experiments, actives, isAuthenticated }
  },
})
</script>
