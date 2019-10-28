<template>
  <v-navigation-drawer
    v-model="drawer"
    :mini-variant.sync="mini"
    :right="right"
    permanent
    width="180"
  >
    <v-list-item @click.stop="mini = !mini">
      <v-list-item-content>
        <v-list-item-title class="font-weight-medium">
          {{ title }} Elements
        </v-list-item-title>
      </v-list-item-content>
    </v-list-item>
    <v-divider />
    <v-list>
      <v-list-item-group v-model="selection">
        <v-divider />
        <template v-for="(element, name) in elements">
          <v-list-item
            :key="name"
            link
            class="left-border"
            :style="`border-color: ${color(element.type)};`"
          >
            <v-list-item-content>
              <v-list-item-title>{{ name }}</v-list-item-title>
              <v-list-item-subtitle>{{ element.type }}</v-list-item-subtitle>
            </v-list-item-content>
          </v-list-item>
          <v-divider :key="`${name}-divider`" />
        </template>
      </v-list-item-group>
    </v-list>
  </v-navigation-drawer>
</template>

<script lang="ts">
import { Component, Vue, Prop } from 'nuxt-property-decorator'

@Component
export default class Elements extends Vue {
  @Prop({ default: false })
  private right!: boolean
  @Prop()
  private title!: string

  private drawer = true
  private mini = false
  private selection?: number | null = null

  private get draft() {
    return this.$accessor.draft.draft
  }
  private get elementTypes() {
    return this.$accessor.draft.elementTypes
  }

  private get elements() {
    if (this.draft) {
      return !this.right ? this.draft.data.before : this.draft.data.after
    }
    return {}
  }

  private color(type: string) {
    return this.$editor.getColor(type)
  }
}
</script>

<style lang="scss" scoped>
.left-border {
  border-left: solid 12px;
}
</style>