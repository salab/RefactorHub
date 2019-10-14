<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator'

@Component
export default class extends Vue {
  private beforeCreate(): void {
    this.$auth.loginWith('github')
  }
}
</script>
