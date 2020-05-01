<script lang="ts">
import { defineComponent, onBeforeMount } from '@vue/composition-api'

export default defineComponent({
  name: 'logout',
  setup(_, { root }) {
    onBeforeMount(() => {
      root.$auth.logout()
    })
  },
})
</script>
