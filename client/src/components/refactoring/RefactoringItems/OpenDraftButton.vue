<template>
  <v-btn outlined tile small class="text-none" @click="open">
    {{ draft.type.name }} (id={{ draft.id
    }}{{ draft.parent ? `, parent=${draft.parent.id}` : ''
    }}{{ draft.origin ? `, origin=${draft.origin.id}` : '' }})
  </v-btn>
</template>

<script lang="ts">
import { defineComponent, useContext } from '@nuxtjs/composition-api'
import { RefactoringDraft } from '@/apis'

export default defineComponent({
  props: {
    draft: {
      type: Object as () => RefactoringDraft,
      required: true,
    },
  },
  setup(props) {
    const {
      app: { router },
    } = useContext()

    const open = () => {
      router?.push(`/draft/${props.draft.id}`)
    }
    return { open }
  },
})
</script>
