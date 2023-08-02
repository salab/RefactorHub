<script setup lang="ts">
import { DiffCategory } from 'refactorhub'
import {
  mdiAsterisk,
  mdiCheckCircle,
  mdiCircleOutline,
  mdiDelete,
} from '@mdi/js'
import { deleteElementDecoration } from '@/components/draft/ElementEditor/ts/elementDecorations'
import apis, { CodeElementHolder, CodeElementMetadata } from '@/apis'

const props = defineProps({
  draftId: {
    type: Number,
    required: true,
  },
  category: {
    type: String as () => DiffCategory,
    required: true,
  },
  elementKey: {
    type: String,
    required: true,
  },
  elementHolder: {
    type: Object as () => CodeElementHolder,
    required: true,
  },
  elementMetadata: {
    type: Object as () => CodeElementMetadata,
    default: null,
  },
  isRemovable: {
    type: Boolean,
    required: true,
  },
})
const isCompleted = computed(() => props.elementHolder.state === 'Manual')

const removeElementKey = async () => {
  if (!confirm('Are you sure you want to delete this element key?')) return
  useDraft().draft.value = (
    await apis.drafts.removeCodeElementHolder(
      props.draftId,
      props.category,
      props.elementKey,
    )
  ).data
  props.elementHolder.elements.forEach((_, i) => {
    deleteElementDecoration(props.category, props.elementKey, i)
  })
}

const verifyElement = async () => {
  useDraft().draft.value = (
    await apis.drafts.verifyCodeElementHolder(
      props.draftId,
      props.category,
      props.elementKey,
      { state: !isCompleted.value },
    )
  ).data
}
</script>

<template>
  <div>
    <v-list-group
      class="element-holder"
      :class="`element-holder-${elementHolder.type}`"
      :value="elementKey"
    >
      <template #activator="{ props: vListGroupProps }">
        <v-list-item v-bind="vListGroupProps" class="pa-0">
          <v-container class="d-flex align-center pa-0 ma-0">
            <div class="mx-1 d-flex flex-column align-center">
              <v-icon
                v-if="elementMetadata && elementMetadata.required"
                class="my-1"
                :size="12"
                color="error"
                title="This element is required"
                :icon="mdiAsterisk"
              />
              <v-btn
                variant="text"
                :size="16"
                icon
                :title="
                  isCompleted
                    ? 'This element is verified'
                    : 'Verify this element'
                "
                @click.stop="verifyElement"
              >
                <v-icon
                  v-if="isCompleted"
                  :size="16"
                  color="success"
                  :icon="mdiCheckCircle"
                />
                <v-icon v-else :size="16" :icon="mdiCircleOutline" />
              </v-btn>
            </div>
            <v-container class="pa-0 ma-0 d-flex flex-column align-left">
              <v-list-item-title
                :title="elementMetadata && elementMetadata.description"
                ><span style="font-size: small">{{
                  elementKey
                }}</span></v-list-item-title
              >
              <v-list-item-subtitle
                ><span style="font-size: x-small">{{
                  `${elementHolder.type}${elementHolder.multiple ? '[]' : ''}`
                }}</span></v-list-item-subtitle
              >
            </v-container>
            <div v-if="isRemovable">
              <v-btn
                variant="text"
                icon
                :size="16"
                color="error"
                @click.stop="removeElementKey"
              >
                <v-icon :size="16" :icon="mdiDelete" />
              </v-btn>
            </div>
          </v-container>
        </v-list-item>
      </template>
      <div v-for="(element, i) in elementHolder.elements" :key="i">
        <v-divider />
        <element-value
          :draft-id="draftId"
          :category="category"
          :element-key="elementKey"
          :element-index="i"
          :element="element"
          :multiple="elementHolder.multiple"
        />
      </div>
      <div v-if="elementHolder.multiple">
        <v-divider />
        <element-value-append-button
          :category="category"
          :element-key="elementKey"
        />
      </div>
    </v-list-group>
    <v-divider />
  </div>
</template>

<style lang="scss" scoped>
.element-holder {
  border-left: solid 0.5rem;
  ::v-deep(&) {
    .v-list-group__header {
      padding: 0;
    }
    .v-list-item__icon.v-list-group__header__append-icon {
      min-width: 0;
      margin: 0 0.5rem;
      .v-icon {
        font-size: 1rem;
      }
    }
  }
}
</style>
