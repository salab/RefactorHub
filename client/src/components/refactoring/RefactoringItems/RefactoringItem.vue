<template>
  <v-card outlined tile class="pa-2">
    <div class="d-flex justify-space-between align-center px-1">
      <div class="title">{{ refactoring.type }} (id={{ refactoring.id }})</div>
      <div class="ml-3">
        <fork-refactoring-button :refactoring="refactoring" />
      </div>
    </div>
    <div class="px-2">
      <div class="body-2">{{ refactoring.description }}</div>
    </div>
    <div v-if="fetchChildren" class="px-2">
      <div class="subtitle-1">Child Refactorings</div>
      <div class="d-flex flex-wrap">
        <div v-for="child in children" :key="child.id" class="ma-1">
          <edit-refactoring-button :refactoring="child" />
        </div>
      </div>
    </div>
    <div v-if="fetchDrafts" class="px-2">
      <div class="subtitle-1">Drafts</div>
      <div class="d-flex flex-wrap">
        <div v-for="draft in drafts" :key="draft.id" class="ma-1">
          <open-draft-button :draft="draft" />
        </div>
      </div>
    </div>
  </v-card>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted } from '@nuxtjs/composition-api'
import { Refactoring, Draft } from 'refactorhub'
import EditRefactoringButton from './EditRefactoringButton.vue'
import ForkRefactoringButton from './ForkRefactoringButton.vue'
import OpenDraftButton from './OpenDraftButton.vue'

export default defineComponent({
  name: 'RefactoringItem',
  components: {
    EditRefactoringButton,
    ForkRefactoringButton,
    OpenDraftButton,
  },
  props: {
    refactoring: {
      type: Object as () => Refactoring,
      required: true,
    },
    fetchChildren: {
      type: Boolean,
      default: false,
    },
    fetchDrafts: {
      type: Boolean,
      default: false,
    },
  },
  setup(props, { root }) {
    const children = ref<Refactoring[]>([])
    const drafts = ref<Draft[]>([])

    onMounted(async () => {
      if (props.fetchChildren) {
        children.value = await root.$client.getRefactoringChildren(
          props.refactoring.id
        )
      }
      // TODO: Fix API
      if (props.fetchDrafts) {
        drafts.value = await root.$client.getRefactoringDrafts(
          props.refactoring.id
        )
      }
    })

    return { children, drafts }
  },
})
</script>