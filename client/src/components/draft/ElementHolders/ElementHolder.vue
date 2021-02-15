<template>
  <div>
    <v-list-group
      class="element-holder"
      :class="`element-holder-${elementHolder.type}`"
      :value="true"
    >
      <template #activator>
        <div class="px-1 d-flex flex-column align-center">
          <div>
            <v-icon
              v-if="elementMetadata && elementMetadata.required"
              x-small
              color="error"
              title="This element is required"
              >fa-fw fa-asterisk</v-icon
            >
          </div>
          <div>
            <v-btn
              x-small
              icon
              :title="
                isCompleted ? 'This element is verified' : 'Verify this element'
              "
              @click.stop="verifyElement"
            >
              <v-icon v-if="isCompleted" small color="success"
                >fa-check-circle</v-icon
              >
              <v-icon v-else small>far fa-circle</v-icon>
            </v-btn>
          </div>
        </div>
        <v-list-item-content
          :title="elementMetadata && elementMetadata.description"
        >
          <v-list-item-title>{{ elementKey }}</v-list-item-title>
          <v-list-item-subtitle>{{
            `${elementHolder.type}${elementHolder.multiple ? '[]' : ''}`
          }}</v-list-item-subtitle>
        </v-list-item-content>
        <div v-if="isRemovable">
          <v-btn icon x-small color="error" @click.stop="removeElementKey">
            <v-icon x-small>fa-trash</v-icon>
          </v-btn>
        </div>
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

<script lang="ts">
import { defineComponent, computed, useContext } from '@nuxtjs/composition-api'
import { DiffCategory } from 'refactorhub'
import { deleteElementDecoration } from '@/components/draft/ElementEditor/ts/elementDecorations'
import apis, { CodeElementHolder, CodeElementMetadata } from '@/apis'

export default defineComponent({
  props: {
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
  },
  setup(props) {
    const {
      app: { $accessor },
    } = useContext()

    const isCompleted = computed(() => props.elementHolder.state === 'Manual')

    const removeElementKey = async () => {
      if (!confirm('Are you sure you want to delete this element key?')) return
      await $accessor.draft.setDraft(
        (
          await apis.drafts.removeCodeElementHolder(
            props.draftId,
            props.category,
            props.elementKey
          )
        ).data
      )
      props.elementHolder.elements.forEach((_, i) => {
        deleteElementDecoration(props.category, props.elementKey, i)
      })
    }

    const verifyElement = async () => {
      await $accessor.draft.setDraft(
        (
          await apis.drafts.verifyCodeElementHolder(
            props.draftId,
            props.category,
            props.elementKey,
            { state: !isCompleted.value }
          )
        ).data
      )
    }
    return { isCompleted, verifyElement, removeElementKey }
  },
})
</script>

<style lang="scss" scoped>
.element-holder {
  border-left: solid 0.5rem;
  &::v-deep {
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
