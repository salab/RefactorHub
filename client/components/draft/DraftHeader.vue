<template>
  <v-app-bar dense>
    <v-spacer />
    <v-btn depressed small color="primary" class="ml-2" @click="save">
      Save
    </v-btn>
    <v-btn depressed small class="ml-2" @click="cancel">
      Cancel
    </v-btn>
  </v-app-bar>
</template>

<script lang="ts">
import { Component, Vue, Prop } from 'nuxt-property-decorator'

@Component
export default class DraftHeader extends Vue {
  @Prop({ required: true })
  private id!: number

  private async save() {
    await this.$client.saveDraft(this.id)
    this.$router.back()
    // TODO
    // const refactoring = await this.$client.saveDraft(this.id)
    // this.$router.push(`/refactoring/${refactoring.id}`)
  }

  private async cancel() {
    await this.$client.cancelDraft(this.id)
    this.$router.back()
  }
}
</script>
