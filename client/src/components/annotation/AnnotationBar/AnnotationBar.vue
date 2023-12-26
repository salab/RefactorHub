<script setup lang="ts">
import { defaultAppBarHeight } from '@/layouts/default.vue'

const experimentId = computed(
  () => useAnnotation().annotation.value?.experimentId,
)
const experimentTitle = ref('')
function updateExperimentTitle() {
  if (!experimentId.value) return
  useExperiment()
    .get(experimentId.value)
    .then((experiment) => (experimentTitle.value = experiment.title))
}
watch(
  () => experimentId.value,
  () => updateExperimentTitle(),
)
const commit = computed(() => useAnnotation().annotation.value?.commit)
</script>
<script lang="ts">
const extensionHeight = 128
export const annotationBarSize = defaultAppBarHeight + extensionHeight
</script>

<template>
  <v-card :height="annotationBarSize" variant="flat">
    <v-app-bar
      :height="defaultAppBarHeight"
      color="primary"
      flat
      :extension-height="extensionHeight"
    >
      <template #prepend>
        <a href="/" class="d-inline-block">
          <v-img src="/logo.png" width="128" />
        </a>
      </template>

      <template #title>
        <div class="d-flex">
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
                title: `${commit.owner}/${
                  commit.repository
                }/${commit.sha.substring(0, 7)}`,
                disabled: true,
                href: `/annotation/${useAnnotation().annotation.value?.id}`,
              },
            ]"
            ><template #divider>
              <v-icon size="small" icon="$mdiChevronRight"></v-icon> </template
          ></v-breadcrumbs>
          <commit-info />
        </div>
      </template>
      <template #extension>
        <div style="display: block; width: 100%">
          <v-divider />
          <div
            :style="`height: ${extensionHeight}px; background-color: ${colors.surface};`"
          >
            <annotation-controller />
          </div>
          <v-divider thickness="3" />
        </div>
      </template>
    </v-app-bar>
  </v-card>
</template>
