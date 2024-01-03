<script setup lang="ts">
import apis from '@/apis'
import { CommonTokenSequenceType } from 'composables/useCommonTokenSequence'

const changeList = computed(() => {
  const list = [...useAnnotation().getChangeList()]
  if (useAnnotation().annotation.value?.hasTemporarySnapshot) list.pop()
  return list
})
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
const hasTemporarySnapshot = computed(
  () => useAnnotation().annotation.value?.hasTemporarySnapshot ?? false,
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
const appendTemporarySnapshot = async () => {
  const { annotation, currentIds } = useAnnotation()
  if (annotation.value?.hasTemporarySnapshot) {
    logger.warn('annotation already has a temporary snapshot')
    return
  }
  if (changeList.value.length === 0) return
  switchCurrentChange(changeList.value[changeList.value.length - 1].id)

  const { annotationId, changeId } = currentIds.value
  if (!annotationId || !changeId) return
  useLoader().setLoadingText('dividing the changes automatically')
  useViewer().deleteNavigators()
  useCommonTokenSequence().updateSelectedId(undefined)
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
  useLoader().finishLoading()
}
const settleTemporarySnapshot = async () => {
  const { annotation, currentIds } = useAnnotation()
  if (!annotation.value?.hasTemporarySnapshot) {
    logger.warn('annotation does not have a temporary snapshot')
    return
  }
  const { annotationId } = currentIds.value
  if (!annotationId) return
  useLoader().setLoadingText('finishing change division')
  useViewer().deleteNavigators()
  useCommonTokenSequence().updateSelectedId(undefined)
  useAnnotation().updateAnnotation(
    {
      ...(await apis.snapshots.settleTemporarySnapshot(annotationId)).data,
    },
    true,
  )
  useLoader().finishLoading()
}
const removeChange = async () => {
  const changeList = useAnnotation().getChangeList()
  if (changeList.length <= 1) {
    logger.warn('cannot remove all changes')
    return
  }
  switchCurrentChange(changeList[changeList.length - 2].id)

  const { currentIds } = useAnnotation()
  const { annotationId, snapshotId, changeId } = currentIds.value
  if (!annotationId || !snapshotId || !changeId) return
  useLoader().setLoadingText('undoing change division')
  useViewer().deleteNavigators()
  useCommonTokenSequence().updateSelectedId(undefined)
  useAnnotation().updateAnnotation(
    {
      ...(await apis.changes.removeChange(annotationId, snapshotId, changeId))
        .data,
    },
    true,
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

const commonTokenSequenceSetting = useCommonTokenSequence().setting
const commonTokenSequenceTypes = [
  'oneToOne',
  'oneToManyOrManyToOne',
  'manyToMany',
] satisfies CommonTokenSequenceType[]
const updateCommonTokensTypes = (types: CommonTokenSequenceType[]) => {
  useCommonTokenSequence().updateSetting({
    oneToOne: types.includes('oneToOne'),
    oneToManyOrManyToOne: types.includes('oneToManyOrManyToOne'),
    manyToMany: types.includes('manyToMany'),
  })
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
            <v-tooltip location="top center" origin="auto" :open-delay="500">
              <template #activator="{ props: tooltipProps }">
                <v-icon
                  v-if="
                    hasTemporarySnapshot &&
                    change.id === changeList[changeList.length - 1]?.id
                  "
                  v-bind="tooltipProps"
                  size="small"
                  icon="$mdiSourceCommitLocal"
                />
              </template>
              Now dividing this change
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
        <span v-else-if="!hasTemporarySnapshot" class="text-body-2"
          >You can <span class="font-weight-medium">finish annotation</span> or
          <span class="font-weight-medium"
            >start change division to continue next annotation</span
          ></span
        >
        <span v-else class="text-body-2"
          ><b>[Change Division in Progress]</b> You can
          <span class="font-weight-medium">finish change division</span> or
          <span class="font-weight-medium"
            >modify the intermediate source code</span
          ></span
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
                      :disabled="hasTemporarySnapshot"
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
                <div v-if="hasTemporarySnapshot" class="font-italic">
                  <hr />
                  You cannot finish annotation while dividing changes <br />
                  Please finish change division first
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
                      :disabled="hasTemporarySnapshot"
                      variant="flat"
                      block
                      size="small"
                      density="comfortable"
                      color="info"
                      prepend-icon="$mdiSourceCommitLocal"
                      @click="() => appendTemporarySnapshot()"
                    >
                      <span class="text-none">Start Change Division</span>
                    </v-btn>
                  </div>
                </template>
                <div>
                  If you have found more than 1 refactoring or functional
                  change, click this button to divide the changes
                </div>
                <div v-if="hasTemporarySnapshot" class="font-italic">
                  <hr />
                  You cannot start another change division while dividing
                  changes
                  <br />
                  Please finish current change division first
                </div>
              </v-tooltip>
            </v-col>
            <v-col class="pl-1">
              <v-tooltip location="top center" origin="auto" :open-delay="500">
                <template #activator="{ props: tooltipProps }">
                  <div v-bind="tooltipProps">
                    <v-btn
                      :disabled="!hasTemporarySnapshot"
                      variant="flat"
                      block
                      size="small"
                      density="comfortable"
                      color="info"
                      prepend-icon="$mdiSourceCommit"
                      ><span class="text-none">Finish Change Division</span>
                      <parameter-dialog
                        title="Confirm Annotations and Source Code"
                        subtitle="[CAUTION] If you have modified the source code, the annotated ranges may be misaligned"
                        :change-parameters-list="[
                          {
                            changeId: changeList[changeList.length - 1].id,
                            parameters: 'all',
                          },
                        ]"
                        :continue-button="{
                          text: 'finish',
                          color: 'info',
                          onClick: () => settleTemporarySnapshot(),
                        }"
                      />
                    </v-btn>
                  </div>
                </template>
                <div>
                  When you have divided the specific change and annotated the
                  parameters, click this button to finish change division
                </div>
                <div v-if="!hasTemporarySnapshot" class="font-italic">
                  <hr />
                  You cannot finish change division because you have not started
                  change division yet
                </div>
              </v-tooltip>
            </v-col>
            <v-col class="pl-1">
              <v-tooltip location="top center" origin="auto" :open-delay="500">
                <template #activator="{ props: tooltipProps }">
                  <div v-bind="tooltipProps">
                    <v-btn
                      :disabled="!hasTemporarySnapshot"
                      variant="flat"
                      block
                      size="small"
                      density="comfortable"
                      color="error"
                      prepend-icon="$mdiDeleteCircle"
                      :onclick="() => removeChange()"
                    >
                      <span class="text-none">Cancel Change Division</span>
                    </v-btn>
                  </div>
                </template>
                <div>
                  If you want to cancel change division, click this button
                  <br />
                  The annotated parameters will be removed
                </div>
                <div v-if="!hasTemporarySnapshot" class="font-italic">
                  <hr />
                  You cannot cancel change division because you have not started
                  change division yet
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
                      :disabled="hasTemporarySnapshot || changeList.length <= 1"
                      variant="flat"
                      block
                      size="small"
                      density="comfortable"
                      color="error"
                      prepend-icon="$mdiDeleteCircle"
                    >
                      <span class="text-none">Remove Latest 2 Annotations</span>
                      <parameter-dialog
                        v-if="changeList.length >= 2"
                        title="Are you sure you want to remove these annotations?"
                        :change-parameters-list="
                          changeList
                            .slice(changeList.length - 2)
                            .map((change) => ({
                              changeId: change.id,
                              parameters: 'all',
                            }))
                        "
                        :continue-button="{
                          text: 'remove',
                          color: 'error',
                          onClick: () => removeChange(),
                        }"
                      />
                    </v-btn>
                  </div>
                </template>
                <div>
                  Undo the latest change division and remove latest 2
                  annotations
                </div>
                <div v-if="hasTemporarySnapshot" class="font-italic">
                  <hr />
                  You cannot remove latest 2 annotations while dividing changes
                  <br />
                  Please finish change division first
                </div>
                <div v-else-if="changeList.length <= 1" class="font-italic">
                  <hr />
                  You cannot remove first annotation
                </div>
              </v-tooltip>
            </v-col>
          </v-row>
          <v-row no-gutters class="pt-1">
            <v-col class="d-flex align-center">
              <v-tooltip location="top center" origin="auto" :open-delay="500">
                <template #activator="{ props: tooltipProps }">
                  <span
                    v-bind="tooltipProps"
                    class="text-subtitle-1 font-weight-medium"
                    >Common Token Sequences Setting</span
                  >
                </template>
                <span class="text-body-2">
                  3 types that have different relationships between
                  <b>the number of occurrences</b> in
                  <b
                    ><span
                      :style="`background-color: ${colors.before}; color: black`"
                    >
                      the code before the change</span
                    ></b
                  >
                  and that in
                  <b
                    ><span
                      :style="`background-color: ${colors.after}; color: black`"
                      >the code after the change</span
                    ></b
                  >
                </span>
              </v-tooltip>
              <span class="text-body-2 font-weight-light mx-2"> (N > 1) </span>
              <v-chip-group
                filter
                multiple
                :model-value="
                  commonTokenSequenceTypes.filter(
                    (type) => commonTokenSequenceSetting[type],
                  )
                "
                class="pa-0"
                @update:model-value="updateCommonTokensTypes"
              >
                <v-tooltip
                  location="top center"
                  origin="auto"
                  :open-delay="500"
                >
                  <template #activator="{ props: tooltipProps }">
                    <v-chip
                      v-bind="tooltipProps"
                      :value="commonTokenSequenceTypes[0]"
                      density="comfortable"
                      >1:1</v-chip
                    >
                  </template>
                  <div>
                    <span
                      :style="`background-color: ${colors.before}; color: black`"
                    >
                      <b>1 occurrence</b> in the code before the change</span
                    >
                    to
                    <span
                      :style="`background-color: ${colors.after}; color: black`"
                    >
                      <b>1 occurrence</b> in the code after the change</span
                    >
                  </div>
                </v-tooltip>
                <v-tooltip
                  location="top center"
                  origin="auto"
                  :open-delay="500"
                >
                  <template #activator="{ props: tooltipProps }">
                    <v-chip
                      v-bind="tooltipProps"
                      :value="commonTokenSequenceTypes[1]"
                      density="comfortable"
                      >1:N & N:1</v-chip
                    >
                  </template>
                  <div>
                    <span
                      :style="`background-color: ${colors.before}; color: black`"
                    >
                      <b>1 occurrence</b> in the code before the change</span
                    >
                    to
                    <span
                      :style="`background-color: ${colors.after}; color: black`"
                    >
                      <b>many occurrence</b> in the code after the change</span
                    >
                  </div>
                  <hr />
                  <div>
                    <span
                      :style="`background-color: ${colors.before}; color: black`"
                    >
                      <b>many occurrence</b> in the code before the change</span
                    >
                    to
                    <span
                      :style="`background-color: ${colors.after}; color: black`"
                    >
                      <b>1 occurrence</b> in the code after the change</span
                    >
                  </div>
                </v-tooltip>
                <v-tooltip
                  location="top center"
                  origin="auto"
                  :open-delay="500"
                >
                  <template #activator="{ props: tooltipProps }">
                    <v-chip
                      v-bind="tooltipProps"
                      :value="commonTokenSequenceTypes[2]"
                      density="comfortable"
                      >N:N</v-chip
                    >
                  </template>
                  <div>
                    <span
                      :style="`background-color: ${colors.before}; color: black`"
                    >
                      <b>many occurrence</b> in the code before the change</span
                    >
                    to
                    <span
                      :style="`background-color: ${colors.after}; color: black`"
                    >
                      <b>many occurrence</b> in the code after the change</span
                    >
                  </div>
                </v-tooltip>
              </v-chip-group>
            </v-col>
          </v-row>
        </div>
      </v-col>
    </v-row>
  </v-container>
</template>
