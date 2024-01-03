<script setup lang="ts">
import apis from '@/apis'

const changeList = computed(() => [...useAnnotation().getChangeList()])
const selectedChangeId = ref(useAnnotation().currentChange.value?.id)
const selectedChange = computed(() =>
  changeList.value.find(({ id }) => id === selectedChangeId.value),
)
const currentChange = computed(() => useAnnotation().currentChange.value)
const changeTypes = computed(() => useAnnotation().changeTypes.value)
const changeTypeVerbTags = computed(() => {
  // HARD CODING
  return [
    'Extract',
    'Move',
    'Rename',
    'Inline',
    'Trim',
    'Reverse',
    'Change',
    'Restructure',
    'Introduce',
    'Expand',
  ]
})
const changeTypeNounTags = computed(() => {
  // HARD CODING
  return [
    'Method',
    'Attribute',
    'Variable',
    'If-Else',
    'Switch',
    'Block',
    'Conditional',
  ]
})
const selectedChangeTypeVerbTags = ref<string[]>([])
const selectedChangeTypeNounTags = ref<string[]>([])
const selectedChangeTypeTags = computed(() => {
  return [
    ...selectedChangeTypeVerbTags.value,
    ...selectedChangeTypeNounTags.value,
  ]
})
const changeDescription = ref(currentChange.value?.description ?? '')

watch(
  () => currentChange.value?.id,
  (newCurrentChangeId) => {
    changeDescription.value = currentChange.value?.description ?? ''
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

async function updateChangeType(typeName: string) {
  const description = currentChange.value?.description ?? ''
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
}
async function updateChangeDescription() {
  const typeName = currentChange.value?.typeName ?? ''
  const description = changeDescription.value
  if (description === currentChange.value?.description) return
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
}

async function createNewChange() {
  const { currentIds } = useAnnotation()
  const { annotationId, snapshotId } = currentIds.value
  if (!annotationId || !snapshotId) return
  useLoader().setLoadingText('creating new annotation')
  useViewer().deleteNavigators()
  useCommonTokenSequence().updateSelectedId(undefined)
  useAnnotation().updateSnapshot(
    {
      ...(await apis.changes.appendChange(annotationId, snapshotId)).data,
    },
    false,
  )
  switchCurrentChange(changeList.value[changeList.value.length - 1].id)
  useLoader().finishLoading()
}
async function removeChange() {
  const changeList = useAnnotation().getChangeList()
  if (changeList.length <= 1) {
    logger.warn('cannot remove all changes')
    return
  }
  const changeIdToBeRemoved = changeList[changeList.length - 1].id
  if (changeIdToBeRemoved === currentChange.value?.id)
    switchCurrentChange(changeList[changeList.length - 2].id)

  const { currentIds } = useAnnotation()
  const { annotationId, snapshotId } = currentIds.value
  if (!annotationId || !snapshotId) return
  useLoader().setLoadingText('removing latest annotation')
  useViewer().deleteNavigators()
  useCommonTokenSequence().updateSelectedId(undefined)
  useAnnotation().updateAnnotation(
    {
      ...(
        await apis.changes.removeChange(
          annotationId,
          snapshotId,
          changeIdToBeRemoved,
        )
      ).data,
    },
    false,
  )
  useLoader().finishLoading()
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

const switchCurrentChange = (newChangeId: string) => {
  if (newChangeId === currentChange.value?.id) return
  useViewer().deleteNavigators()
  useCommonTokenSequence().updateSelectedId(undefined)
  useAnnotation().updateCurrentChangeId(newChangeId)
}
</script>

<template>
  <v-container fluid class="pa-1" style="height: 100%">
    <v-row no-gutters>
      <v-col cols="6" class="pr-1">
        <v-tabs
          v-model="selectedChangeId"
          center-active
          show-arrows
          density="compact"
          bg-color="primary"
          height="25"
        >
          <v-tab
            v-for="change in changeList"
            :key="change.id"
            :value="change.id"
            :onclick="() => switchCurrentChange(change.id)"
          >
            <v-tooltip location="top center" origin="auto" :open-delay="500">
              <template #activator="{ props: tooltipProps }">
                <v-icon
                  v-if="change.id === currentChange?.id"
                  v-bind="tooltipProps"
                  size="small"
                  icon="$mdiMarker"
                />
              </template>
              Now displaying this change
            </v-tooltip>
            <span
              v-if="change.id === currentChange?.id"
              class="text-none font-weight-black"
              >{{ change.typeName }}</span
            >
            <span v-else class="text-none"> {{ change.typeName }}</span>
            <v-tooltip location="top center" origin="auto" :open-delay="500">
              <template #activator="{ props: tooltipProps }">
                <v-btn
                  v-bind="tooltipProps"
                  variant="text"
                  size="20"
                  icon
                  density="comfortable"
                >
                  <v-icon size="20" icon="$mdiInformation" color="info" />
                  <parameter-dialog
                    title="Annotated Parameters"
                    :subtitle="change.description"
                    :change-parameters-list="[
                      {
                        changeId: change.id,
                        parameters: 'all',
                      },
                    ]"
                    :continue-button="
                      currentChange?.id !== change.id
                        ? {
                            text: 'switch',
                            color: 'success',
                            onClick: () => {
                              switchCurrentChange(change.id)
                            },
                          }
                        : undefined
                    "
                  />
                </v-btn>
              </template>
              Show Annotated Parameters
            </v-tooltip>
          </v-tab>
        </v-tabs>
        <v-row class="mt-0">
          <v-col cols="3">
            <v-tooltip
              v-if="changeTypes && selectedChange"
              location="top center"
              origin="auto"
              :open-delay="500"
            >
              <template #activator="{ props: tooltipProps }">
                <div v-bind="tooltipProps">
                  <v-select
                    variant="underlined"
                    density="compact"
                    :model-value="selectedChange.typeName"
                    :disabled="!isOwner || !isDraft"
                    :hide-details="true"
                    :items="
                      changeTypes
                        .filter((type) =>
                          selectedChangeTypeTags.every((tag) =>
                            type.tags.includes(tag),
                          ),
                        )
                        .map((type) => type.name)
                    "
                    :label="`Change Type (${
                      changeTypes.filter((type) =>
                        selectedChangeTypeTags.every((tag) =>
                          type.tags.includes(tag),
                        ),
                      ).length
                    } / ${changeTypes.length})`"
                    @update:model-value="
                      (newTypeName) => updateChangeType(newTypeName)
                    "
                  />
                </div>
              </template>
              {{ selectedChange.typeName }}
            </v-tooltip>
          </v-col>
          <v-col class="pa-0">
            <div>
              <div
                class="text-caption"
                :style="`color: ${vuetifyColors.grey.darken1}`"
              >
                Change Type Filter
              </div>
              <div class="ml-2 d-flex align-center">
                <div
                  :style="`color: ${vuetifyColors.grey.darken1}`"
                  style="font-size: xx-small"
                  class="mr-1"
                >
                  Operation
                </div>
                <v-chip-group
                  v-model="selectedChangeTypeVerbTags"
                  column
                  multiple
                  filter
                  class="pa-0"
                >
                  <v-chip
                    v-for="tag in changeTypeVerbTags"
                    :key="tag"
                    :value="tag"
                    filter
                    density="compact"
                    size="small"
                    variant="tonal"
                    class="px-1 py-0 ml-1 mr-0 my-0"
                  >
                    {{ tag }}</v-chip
                  >
                </v-chip-group>
              </div>
              <div class="ml-2 d-flex align-center">
                <div
                  :style="`color: ${vuetifyColors.grey.darken1}`"
                  style="font-size: xx-small"
                  class="mr-1"
                >
                  Code Element
                </div>
                <v-chip-group
                  v-model="selectedChangeTypeNounTags"
                  column
                  multiple
                  filter
                  class="pa-0"
                >
                  <v-chip
                    v-for="tag in changeTypeNounTags"
                    :key="tag"
                    :value="tag"
                    filter
                    density="compact"
                    size="small"
                    variant="tonal"
                    class="px-1 py-0 ml-1 mr-0 my-0"
                  >
                    {{ tag }}</v-chip
                  >
                </v-chip-group>
              </div>
            </div>
          </v-col>
        </v-row>
        <v-row class="pa-0 ma-0">
          <v-col class="pa-0 mt-2">
            <v-textarea
              variant="underlined"
              density="compact"
              :model-value="changeDescription"
              :disabled="!isOwner || !isDraft"
              :hide-details="true"
              rows="1"
              label="Description"
              @update:model-value="
                (newDescription) => (changeDescription = newDescription)
              "
              @update:focused="
                (focused) => {
                  if (!focused) updateChangeDescription()
                }
              "
            />
          </v-col>
        </v-row>
      </v-col>
      <v-col cols="6">
        <span v-if="!isOwner" class="text-body-2"
          ><b>[Readonly]</b> You can view the annotation but cannot edit
          it</span
        >
        <span v-else-if="!isDraft" class="text-body-2"
          ><b>[Readonly]</b> You can modify the annotation if you want</span
        >
        <span v-else class="text-body-2"
          >You can <span class="font-weight-medium">finish annotation</span> or
          <span class="font-weight-medium">annotate new change</span></span
        >
        <div v-if="isOwner && !isDraft">
          <v-row no-gutters class="pa-0">
            <v-col>
              <v-btn
                variant="flat"
                block
                size="small"
                density="comfortable"
                color="secondary"
                @click="() => updateIsDraft(true)"
                ><span class="text-none"
                  >Start Modifying Annotation</span
                ></v-btn
              >
            </v-col>
          </v-row>
        </div>
        <div v-if="isOwner && isDraft">
          <v-row no-gutters class="pa-0">
            <v-col>
              <v-tooltip location="top center" origin="auto" :open-delay="500">
                <template #activator="{ props: tooltipProps }">
                  <div v-bind="tooltipProps">
                    <v-btn
                      variant="flat"
                      block
                      size="small"
                      density="comfortable"
                      color="success"
                      prepend-icon="$mdiCheckCircle"
                      ><span class="text-none">Finish Annotation</span>
                      <parameter-dialog
                        title="Confirm Annotations"
                        :change-parameters-list="
                          changeList.map((change) => ({
                            changeId: change.id,
                            parameters: 'all',
                          }))
                        "
                        :continue-button="{
                          text: 'finish',
                          color: 'success',
                          onClick: () => updateIsDraft(false),
                        }"
                      />
                    </v-btn>
                  </div>
                </template>
                <div>
                  When you have annotated all the refactorings which the commit
                  has, click this button
                </div>
              </v-tooltip>
            </v-col>
          </v-row>
          <v-row no-gutters class="pt-1">
            <v-col>
              <v-tooltip location="top center" origin="auto" :open-delay="500">
                <template #activator="{ props: tooltipProps }">
                  <div v-bind="tooltipProps">
                    <v-btn
                      variant="flat"
                      block
                      size="small"
                      density="comfortable"
                      color="info"
                      prepend-icon="$mdiPlusCircle"
                      @click="() => createNewChange()"
                    >
                      <span class="text-none">Annotate New Change</span>
                    </v-btn>
                  </div>
                </template>
                <div>Click this button to annotate another change</div>
              </v-tooltip>
            </v-col>
            <v-col class="pl-1">
              <v-tooltip location="top center" origin="auto" :open-delay="500">
                <template #activator="{ props: tooltipProps }">
                  <div v-bind="tooltipProps">
                    <v-btn
                      :disabled="changeList.length <= 1"
                      variant="flat"
                      block
                      size="small"
                      density="comfortable"
                      color="error"
                      prepend-icon="$mdiDeleteCircle"
                      :onclick="() => removeChange()"
                    >
                      <span class="text-none">Remove Latest Annotation</span>
                    </v-btn>
                  </div>
                </template>
                <div>
                  If you want to remove annotations you made, click this button
                  to remove the latest annotation
                </div>
                <div v-if="changeList.length <= 1" class="font-italic">
                  <hr />
                  You cannot remove first annotation
                </div>
              </v-tooltip>
            </v-col>
          </v-row>
        </div>
      </v-col>
    </v-row>
  </v-container>
</template>
