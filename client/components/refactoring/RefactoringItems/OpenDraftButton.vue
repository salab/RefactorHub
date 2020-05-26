<template>
  <v-btn outlined tile small class="text-none" @click="open">
    {{ draft.type.name }} (id={{ draft.id
    }}{{ draft.parent ? `, parent=${draft.parent.id}` : ''
    }}{{ draft.origin ? `, origin=${draft.origin.id}` : '' }})
  </v-btn>
</template>

<script lang="ts">
import { defineComponent, PropType } from '@vue/composition-api'
import { Draft } from 'refactorhub'

export default defineComponent({
  name: 'OpenDraftButton',
  props: {
    draft: {
      type: Object as PropType<Draft>,
      required: true,
    },
  },
  setup(props, { root }) {
    const open = () => {
      root.$router.push(`/draft/${props.draft.id}`)
    }
    return { open }
  },
})
</script>
