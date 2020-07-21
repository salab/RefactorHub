<template>
  <v-container fill-height>
    <v-col>
      <link-buttons />
    </v-col>
  </v-container>
</template>

<script lang="ts">
import { defineComponent } from '@vue/composition-api'
import LinkButtons from '@/components/common/LinkButtons.vue'

export default defineComponent({
  name: 'home',
  components: {
    LinkButtons,
  },
})
</script>
