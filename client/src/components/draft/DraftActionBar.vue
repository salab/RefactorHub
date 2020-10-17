<template>
  <v-card flat tile>
    <v-app-bar dense flat>
      <v-spacer />
      <v-btn depressed small tile color="primary" class="ml-2" @click="save">
        save
      </v-btn>
      <v-btn depressed small tile text class="ml-2" @click="discard">
        discard
      </v-btn>
    </v-app-bar>
  </v-card>
</template>

<script lang="ts">
import { defineComponent } from '@nuxtjs/composition-api'

export default defineComponent({
  props: {
    save: {
      type: Function,
      required: true,
    },
    discard: {
      type: Function,
      required: true,
    },
  },
})
</script>
