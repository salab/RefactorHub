<script setup lang="ts">
defineProps({
  save: {
    type: Function,
    required: true,
  },
  discard: {
    type: Function,
    required: true,
  },
})
</script>

<template>
  <v-card :height="45">
    <v-app-bar :height="45" color="grey-lighten-4" app flat>
      <v-spacer />
      <v-btn
        variant="flat"
        size="small"
        :rounded="false"
        color="primary"
        class="ml-2"
        @click="save"
      >
        <span class="text-none">Save</span>
      </v-btn>
      <v-btn
        variant="flat"
        size="small"
        :rounded="false"
        color="error"
        class="ml-2"
        @click="discard"
      >
        <span class="text-none">Discard</span>
      </v-btn>
    </v-app-bar>
  </v-card>
</template>
