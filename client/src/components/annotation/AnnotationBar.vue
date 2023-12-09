<script setup lang="ts">
import { DiffCategory } from 'refactorhub'

defineProps({
  isActiveOfElementHolders: {
    type: Object as () => { [category in DiffCategory]: boolean },
    required: true,
  },
})

const experimentId = computed(
  () => useAnnotation().annotation.value?.experimentId,
)
const experimentTitle = ref('')
function updateExperimentTitle() {
  if (!experimentId.value) return
  useExperiment()
    .getExperiment(experimentId.value)
    .then((experiment) => (experimentTitle.value = experiment.title))
}
watch(
  () => experimentId.value,
  () => updateExperimentTitle(),
)
const commit = computed(() => useAnnotation().annotation.value?.commit)

const emit = defineEmits<{
  (event: 'toggleElementHolders', category: DiffCategory): void
}>()
</script>

<template>
  <v-card :height="45">
    <v-app-bar :height="45" color="primary" app flat class="d-flex">
      <v-btn
        variant="flat"
        size="small"
        :color="colors.before"
        :text="
          isActiveOfElementHolders.before ? 'hide' : 'open before parameters'
        "
        :prepend-icon="
          isActiveOfElementHolders.before
            ? '$mdiArrowCollapseLeft'
            : '$mdiArrowExpandRight'
        "
        @click="emit('toggleElementHolders', 'before')"
      />
      <v-spacer />
      <a href="/" class="align-self-end mb-1 d-inline-block">
        <v-img src="/icon.png" width="32" height="32"></v-img>
      </a>
      <v-breadcrumbs
        v-if="experimentId && experimentTitle && commit"
        class="align-self-end pa-0"
        style="font-size: small"
        :items="[
          { title: 'Experiments', disabled: false, href: '/experiment' },
          {
            title: experimentTitle,
            disabled: false,
            href: `/experiment/${experimentId}`,
          },
          {
            title: `${commit.owner}/${commit.repository}/${commit.sha.substring(
              0,
              7,
            )}`,
            disabled: true,
            href: `/annotation/${useAnnotation().annotation.value?.id}`,
          },
        ]"
        ><template #divider>
          <v-icon size="small" icon="$mdiChevronRight"></v-icon> </template
      ></v-breadcrumbs>
      <v-spacer />
      <v-btn
        variant="flat"
        size="small"
        :color="colors.after"
        :text="
          isActiveOfElementHolders.after ? 'hide' : 'open after parameters'
        "
        :append-icon="
          isActiveOfElementHolders.after
            ? '$mdiArrowCollapseRight'
            : '$mdiArrowExpandLeft'
        "
        @click="emit('toggleElementHolders', 'after')"
      />
    </v-app-bar>
  </v-card>
</template>
