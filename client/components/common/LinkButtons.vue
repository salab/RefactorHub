<template>
  <v-row justify="center">
    <div v-for="link in links" :key="link.to" class="ma-2">
      <v-btn :to="link.to" class="text-none">
        {{ link.name }}
      </v-btn>
    </div>
  </v-row>
</template>

<script lang="ts">
import { defineComponent } from '@vue/composition-api'

const links = [
  { name: 'Tutorial', to: '/tutorial' },
  { name: 'Experiment', to: '/experiment' },
]

export default defineComponent({
  name: 'LinkButtons',
  setup() {
    return { links }
  },
})
</script>
