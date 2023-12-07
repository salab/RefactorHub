<script setup lang="ts">
import { DiffCategory } from 'refactorhub'

defineProps({
  suspend: {
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
      <v-btn variant="flat" size="small" color="secondary" @click="suspend">
        <span class="text-none">Suspend Annotation</span>
      </v-btn>
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
