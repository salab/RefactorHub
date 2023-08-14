<script setup lang="ts">
import { DiffCategory } from 'refactorhub'

defineProps({
  save: {
    type: Function,
    required: true,
  },
  discard: {
    type: Function,
    required: true,
  },
  isActiveOfElementHolders: {
    type: Object as () => { [category in DiffCategory]: boolean },
    required: true,
  },
})

const emit = defineEmits<{
  (event: 'toggleElementHolders', category: DiffCategory): void
}>()
</script>

<template>
  <v-card :height="45">
    <v-app-bar :height="45" color="primary" app flat>
      <v-btn
        variant="flat"
        size="small"
        :color="colors.before"
        :text="
          isActiveOfElementHolders.before ? 'hide' : 'open before elements'
        "
        :prepend-icon="
          isActiveOfElementHolders.before
            ? '$mdiArrowCollapseLeft'
            : '$mdiArrowExpandRight'
        "
        @click="emit('toggleElementHolders', 'before')"
      />
      <v-spacer />
      <v-btn variant="flat" size="small" color="success" @click="save">
        <span class="text-none">Save</span>
      </v-btn>
      <v-btn variant="flat" size="small" color="error" @click="discard">
        <span class="text-none">Discard</span>
      </v-btn>
      <v-spacer />
      <v-btn
        variant="flat"
        size="small"
        :color="colors.after"
        :text="isActiveOfElementHolders.after ? 'hide' : 'open after elements'"
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
