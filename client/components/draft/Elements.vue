<template>
  <v-navigation-drawer
    v-model="drawer"
    :mini-variant.sync="mini"
    :right="right"
    permanent
    width="180"
  >
    <v-list-item @click.stop="mini = !mini">
      <v-list-item-title>{{ title }} Elements</v-list-item-title>
      <v-icon v-if="right" small>fa-chevron-right</v-icon>
      <v-icon v-else small>fa-chevron-left</v-icon>
    </v-list-item>
    <v-divider></v-divider>
    <v-list dense>
      <v-list-item v-for="element in elements" :key="element.name" link>
        <v-list-item-content>
          <v-list-item-title>{{ element.name }}</v-list-item-title>
        </v-list-item-content>
      </v-list-item>
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
  private elements = [{ name: 'elementName' }]
}
</script>
