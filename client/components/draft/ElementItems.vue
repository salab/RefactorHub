<template>
  <v-navigation-drawer
    v-model="drawer"
    :mini-variant.sync="mini"
    :right="diff === 'after'"
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
            class="element-item"
            :class="`element-item-${element.type}`"
          >
            <v-list-item-content>
              <v-list-item-title>{{ name }}</v-list-item-title>
              <v-list-item-subtitle>{{ element.type }}</v-list-item-subtitle>
            </v-list-item-content>
            <v-list-item-action class="ml-0">
              <v-icon
                v-if="!element.incomplete"
                small
                color="success"
                title="Completed"
                >fa-check</v-icon
              >
              <v-icon v-else small color="error" title="This is not completed"
                >fa-question</v-icon
              >
            </v-list-item-action>
          </v-list-item>
          <v-divider :key="`${name}-divider`" />
        </template>
      </v-list-item-group>
    </v-list>
  </v-navigation-drawer>
</template>

<script lang="ts">
import { Component, Vue, Prop, Watch } from 'nuxt-property-decorator'
import { Diff } from 'refactorhub'

@Component
export default class ElementItems extends Vue {
  @Prop({ default: 'before' })
  private diff!: Diff

  private drawer = true
  private mini = false
  private selection?: number = -1

  @Watch('selection')
  private onChangeSelection(value?: number) {
    this.$accessor.draft.setElement({
      diff: this.diff,
      element:
        value !== undefined ? Object.entries(this.elements)[value] : undefined
    })
  }

  private get draft() {
    return this.$accessor.draft.draft
  }

  private get elementTypes() {
    return this.$accessor.draft.elementTypes
  }

  private get elements() {
    if (this.draft) {
      return this.diff === 'before'
        ? this.draft.data.before
        : this.draft.data.after
    }
    return {}
  }

  private get title() {
    return this.diff === 'before' ? 'Before' : 'After'
  }
}
</script>

<style lang="scss" scoped>
.element-item {
  border-left: solid 12px;
  padding: 0 8px;
}
</style>
