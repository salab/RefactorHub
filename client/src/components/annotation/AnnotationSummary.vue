<script setup lang="ts">
import { debounce } from 'lodash-es'
import apis from '@/apis'

const changeList = computed(() => useAnnotation().getChangeList())
const selectedChangeId = ref(useAnnotation().currentChange.value?.id)
const selectedChange = computed(() =>
  changeList.value.find(({ id }) => id === selectedChangeId.value),
)
const currentChange = computed(() => useAnnotation().currentChange.value)
const changeTypes = computed(() => useAnnotation().changeTypes.value)

watch(
  () => currentChange.value?.id,
  (newCurrentChangeId) => {
    // NOTE: there is not newCurrentChangeId in v-tabs of changes when change is removed, so reload twice
    selectedChangeId.value = newCurrentChangeId
    setTimeout(() => (selectedChangeId.value = newCurrentChangeId), 1)
  },
)

const isDraft = computed(
  () => useAnnotation().annotation.value?.isDraft ?? false,
)
const isOwner = computed(
  () =>
    (useAnnotation().annotation.value?.ownerId ?? '') ===
    useUser().user.value?.id,
)
const hasTemporarySnapshot = computed(
  () => useAnnotation().annotation.value?.hasTemporarySnapshot ?? false,
)
const currentIsLast = computed(
  () =>
    changeList.value.length > 0 &&
    changeList.value[changeList.value.length - 1].id ===
      currentChange.value?.id,
)
const currentIsSecondFromLast = computed(
  () =>
    changeList.value.length > 1 &&
    changeList.value[changeList.value.length - 2].id ===
      currentChange.value?.id,
)

const updateChange = debounce(
  async (
    typeName: string = currentChange.value?.typeName ?? '',
    description: string = currentChange.value?.description ?? '',
  ) => {
    const { annotationId, snapshotId, changeId } =
      useAnnotation().currentIds.value
    if (!annotationId || !snapshotId || !changeId) return
    useAnnotation().updateChange(
      (
        await apis.changes.updateChange(annotationId, snapshotId, changeId, {
          typeName,
          description,
        })
      ).data,
    )
  },
  500,
)
const appendTemporarySnapshot = async () => {
  const { annotation, currentIds } = useAnnotation()
  if (annotation.value?.hasTemporarySnapshot) {
    logger.warn('annotation already has a temporary snapshot')
    return
  }
  if (!currentIsLast.value) {
    logger.warn('only trailing change can be extracted')
    return
  }
  const { annotationId, changeId } = currentIds.value
  if (!annotationId || !changeId) return
  useAnnotation().updateAnnotation(
    {
      ...(
        await apis.snapshots.appendTemporarySnapshot(annotationId, {
          changeId,
        })
      ).data,
    },
    true,
  )
}
const settleTemporarySnapshot = async () => {
  const { annotation, currentIds } = useAnnotation()
  if (!annotation.value?.hasTemporarySnapshot) {
    logger.warn('annotation does not have a temporary snapshot')
    return
  }
  const { annotationId } = currentIds.value
  if (!annotationId) return
  useAnnotation().updateAnnotation(
    {
      ...(await apis.snapshots.settleTemporarySnapshot(annotationId)).data,
    },
    true,
  )
}
const removeChange = async () => {
  const { currentIds } = useAnnotation()
  if (!currentIsSecondFromLast.value) {
    logger.warn('only the change which is second from last can be removed')
    return
  }
  const { annotationId, snapshotId, changeId } = currentIds.value
  if (!annotationId || !snapshotId || !changeId) return
  useAnnotation().updateAnnotation(
    {
      ...(await apis.changes.removeChange(annotationId, snapshotId, changeId))
        .data,
    },
    true,
  )
}
const updateIsDraft = async (isDraft: boolean) => {
  const { annotationId } = useAnnotation().currentIds.value
  if (!annotationId) return
  useAnnotation().updateAnnotation(
    {
      ...(await apis.annotations.publishAnnotation(annotationId, { isDraft }))
        .data,
    },
    false,
  )
}

const switchCurrentChange = () => {
  if (
    !selectedChangeId.value ||
    selectedChangeId.value === currentChange.value?.id
  )
    return
  useAnnotation().updateCurrentChangeId(selectedChangeId.value)
}
</script>

<template>
  <v-expansion-panels>
    <v-expansion-panel :elevation="0" :rounded="0" class="info-panel">
      <v-expansion-panel-title>
        <div class="d-flex">
          <div class="flex-grow-0 d-flex align-center pr-3">
            <h2>{{ currentChange?.typeName }}</h2>
          </div>
          <div class="d-flex align-center">
            {{ currentChange?.description }}
          </div>
        </div>
      </v-expansion-panel-title>
      <v-expansion-panel-text>
        <v-container fluid class="pa-0">
          <h3>Annotation</h3>
          <v-container fluid class="d-flex pa-0 py-1">
            <span v-if="!isOwner" class="text-body-2"
              ><b>[Readonly]</b> You can view this annotation but cannot edit
              it</span
            >
            <span v-if="isOwner && !isDraft" class="text-body-2"
              ><b>[Readonly]</b> You can start annotation if you want to modify
              it</span
            >
            <span
              v-if="isOwner && isDraft && !hasTemporarySnapshot"
              class="text-body-2"
              >You can
              <span class="font-weight-medium">finish annotation</span> or
              <span class="font-weight-medium"
                >extract trailing annotated change to continue next
                annotation</span
              ></span
            >
            <span
              v-if="isOwner && isDraft && hasTemporarySnapshot"
              class="text-body-2"
              ><b>[Extraction in Progress]</b> You can
              <span class="font-weight-medium">finish extraction</span> or
              <span class="font-weight-medium"
                >modify the extracted source codes</span
              ></span
            >
            <v-spacer />
            <v-btn
              v-if="isOwner && !isDraft"
              variant="flat"
              size="small"
              density="comfortable"
              color="secondary"
              @click="() => updateIsDraft(true)"
              ><span class="text-none">Start Modifying Annotation</span></v-btn
            >
            <v-btn
              v-if="isOwner && isDraft && !hasTemporarySnapshot"
              variant="flat"
              size="small"
              density="comfortable"
              color="success"
              @click="() => updateIsDraft(false)"
              ><span class="text-none">Finish Annotation</span></v-btn
            >
            <v-btn
              v-if="isOwner && isDraft && hasTemporarySnapshot"
              variant="flat"
              size="small"
              density="comfortable"
              color="info"
              prepend-icon="$mdiSourceCommit"
              @click="() => settleTemporarySnapshot()"
              ><span class="text-none">Finish Extraction</span></v-btn
            >
          </v-container>
          <v-tabs
            v-model="selectedChangeId"
            center-active
            show-arrows
            density="compact"
            bg-color="primary"
          >
            <v-tab
              v-for="change in changeList"
              :key="change.id"
              :value="change.id"
            >
              <v-icon
                v-if="change.id === currentChange?.id"
                size="small"
                icon="$mdiMarker"
              />
              <span
                v-if="change.id === currentChange?.id"
                class="text-none font-weight-black"
                >{{ change.typeName }}</span
              >
              <span v-else class="text-none"> {{ change.typeName }}</span>
            </v-tab>
          </v-tabs>
          <v-row class="pt-3">
            <v-col>
              <v-select
                v-if="changeTypes && selectedChange"
                variant="underlined"
                density="compact"
                :model-value="selectedChange.typeName"
                :disabled="
                  !isOwner ||
                  !isDraft ||
                  selectedChangeId !== currentChange?.id ||
                  (hasTemporarySnapshot && currentIsLast)
                "
                :hide-details="true"
                :items="changeTypes.map((it) => it.name)"
                label="Change Type"
                @update:model-value="
                  (newTypeName) =>
                    updateChange(newTypeName, currentChange?.description)
                "
              />
            </v-col>
            <v-col>
              <v-textarea
                variant="underlined"
                density="compact"
                :model-value="selectedChange?.description"
                :disabled="
                  !isOwner ||
                  !isDraft ||
                  selectedChangeId !== currentChange?.id ||
                  (hasTemporarySnapshot && currentIsLast)
                "
                :hide-details="true"
                rows="2"
                label="Description"
                @update:model-value="
                  (newDescription) =>
                    updateChange(currentChange?.typeName, newDescription)
                "
              />
            </v-col>
            <v-col v-if="isOwner">
              <v-container
                v-if="isDraft && !hasTemporarySnapshot"
                fluid
                class="d-flex flex-column ma-0 pa-0"
              >
                <v-btn
                  v-if="currentIsLast && selectedChangeId === currentChange?.id"
                  variant="flat"
                  size="small"
                  density="comfortable"
                  color="info"
                  prepend-icon="$mdiSourceCommitLocal"
                  class="my-1"
                  @click="() => appendTemporarySnapshot()"
                >
                  <span class="text-none"
                    >Extract Annotated Change for Next Annotation</span
                  >
                </v-btn>
                <v-btn
                  v-if="
                    currentIsSecondFromLast &&
                    selectedChangeId === currentChange?.id
                  "
                  variant="flat"
                  size="small"
                  density="comfortable"
                  color="warning"
                  prepend-icon="$mdiDeleteCircle"
                  class="my-1"
                  @click="() => removeChange()"
                >
                  <span class="text-none"
                    >Reset Annotations after This</span
                  ></v-btn
                >
                <v-btn
                  v-if="selectedChangeId !== currentChange?.id"
                  variant="flat"
                  size="small"
                  density="comfortable"
                  color="primary"
                  prepend-icon="$mdiMarker"
                  class="my-1"
                  @click="() => switchCurrentChange()"
                  ><span class="text-none"
                    >Switch Displaying Change</span
                  ></v-btn
                >
              </v-container>
              <v-container
                v-if="isDraft && hasTemporarySnapshot"
                fluid
                class="d-flex flex-column ma-0 pa-0"
              >
                <v-btn
                  v-if="
                    currentIsSecondFromLast &&
                    selectedChangeId === currentChange?.id
                  "
                  variant="flat"
                  size="small"
                  density="comfortable"
                  color="warning"
                  prepend-icon="$mdiDeleteCircle"
                  class="my-1"
                  @click="() => removeChange()"
                  ><span class="text-none"
                    >Reset Annotations after This</span
                  ></v-btn
                >
                <v-btn
                  v-if="selectedChangeId !== currentChange?.id"
                  variant="flat"
                  size="small"
                  density="comfortable"
                  color="primary"
                  prepend-icon="$mdiMarker"
                  class="my-1"
                  @click="() => switchCurrentChange()"
                  ><span class="text-none"
                    >Switch Displaying Change</span
                  ></v-btn
                >
              </v-container>
              <v-container
                v-if="!isDraft"
                fluid
                class="d-flex flex-column ma-0 pa-0"
              >
                <v-btn
                  v-if="selectedChangeId !== currentChange?.id"
                  variant="flat"
                  size="small"
                  density="comfortable"
                  color="primary"
                  prepend-icon="$mdiMarker"
                  class="my-1"
                  @click="() => switchCurrentChange()"
                  ><span class="text-none"
                    >Switch Displaying Change</span
                  ></v-btn
                >
              </v-container>
            </v-col>
          </v-row>
        </v-container>
      </v-expansion-panel-text>
    </v-expansion-panel>
  </v-expansion-panels>
</template>

<style lang="scss" scoped>
.info-panel {
  ::v-deep(&) {
    .v-expansion-panel-text__wrap {
      padding: 0;
    }
    .v-expansion-panel-title {
      min-height: 2.4rem;
      padding: 0 0.8rem;
      .v-expansion-panel-title__icon {
        .v-icon {
          font-size: 1.2rem;
        }
      }
    }
  }
}
</style>
