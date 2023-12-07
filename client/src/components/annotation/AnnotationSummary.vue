<script setup lang="ts">
import { debounce } from 'lodash-es'
import apis from '@/apis'

const currentChange = computed(() => useAnnotation().currentChange.value)
const changeTypes = computed(() => useAnnotation().changeTypes.value)

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
          <v-select
            v-if="changeTypes"
            variant="underlined"
            :model-value="currentChange?.typeName"
            :items="changeTypes.map((it) => it.name)"
            label="Refactoring Type"
            @update:model-value="
              (newTypeName) =>
                updateChange(newTypeName, currentChange?.description)
            "
          />
          <v-textarea
            variant="underlined"
            :model-value="currentChange?.description"
            rows="2"
            label="Description"
            @update:model-value="
              (newDescription) =>
                updateChange(currentChange?.typeName, newDescription)
            "
          />
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
