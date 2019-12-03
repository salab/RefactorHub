<template>
  <v-layout column fill-height>
    <v-flex>
      <info />
    </v-flex>
    <v-flex xs12>
      <v-layout fill-height>
        <v-flex>
          <elements />
        </v-flex>
        <v-flex xs12>
          <v-layout column fill-height>
            <v-flex xs12>
              <monaco-editor ref="editor" />
            </v-flex>
            <v-flex>
              <files />
            </v-flex>
          </v-layout>
        </v-flex>
        <v-flex>
          <elements :diff="'after'" />
        </v-flex>
      </v-layout>
    </v-flex>
    <component :is="'style'">
      <!-- prettier-ignore -->
      <template v-for="type in elementTypes">
        .element-widget-{{ type }} {
          background-color: {{ $editor.getColor(type, 0.2) }};
          color: {{ $editor.getColor(type, 0.9) }};
        }
        .element-item-{{ type }} {
          border-color: {{ $editor.getColor(type) }} !important;
        }
        .element-decoration-{{ type }} {
          background-color: {{ $editor.getColor(type, 0.2) }};
          border-color: {{ $editor.getColor(type, 0.9) }} !important;
        }
      </template>
    </component>
  </v-layout>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'nuxt-property-decorator'
import { Diff, Element } from 'refactorhub'
import Elements from '~/components/draft/Elements.vue'
import Files from '~/components/draft/Files.vue'
import Info from '~/components/draft/Info.vue'
import MonacoEditor from '~/components/draft/MonacoEditor.vue'

@Component({
  components: {
    Elements,
    Files,
    Info,
    MonacoEditor
  }
})
export default class extends Vue {
  $refs!: {
    editor: MonacoEditor
  }

  private get draft() {
    return this.$accessor.draft.draft
  }

  private get commit() {
    return this.$accessor.draft.commit
  }

  private get file() {
    return this.$accessor.draft.file
  }

  private get element() {
    return this.$accessor.draft.element
  }

  private get elementTypes() {
    return this.$accessor.draft.elementTypes
  }

  private async mounted() {
    await Promise.all([
      this.$accessor.draft.fetchDraft(parseInt(this.$route.params.id)),
      this.$accessor.draft.fetchRefactoringTypes(),
      this.$accessor.draft.fetchElementTypes()
    ])
    this.$accessor.draft.setFile({ diff: 'before', value: 0 })
    this.$accessor.draft.setFile({ diff: 'after', value: 0 })
  }

  @Watch('file.before')
  private async onChangeFileBefore(value?: number) {
    await this.onChangeFile('before', value)
  }

  @Watch('file.after')
  private async onChangeFileAfter(value?: number) {
    await this.onChangeFile('after', value)
  }

  private async onChangeFile(diff: Diff, value?: number) {
    if (this.commit && value !== undefined) {
      const path =
        diff === 'before'
          ? this.commit.files[value].previousName
          : this.commit.files[value].name
      await this.$refs.editor.setTextModel(
        diff,
        this.commit.owner,
        this.commit.repository,
        diff === 'before' ? this.commit.parent : this.commit.sha,
        path
      )
      const draft = this.$accessor.draft.draft
      if (draft) {
        Object.entries(draft.data[diff]).forEach(([key, element]) => {
          if (path === element.location.path) {
            this.$refs.editor.setDecoration(diff, key, element)
          }
        })
      }
    }
  }

  @Watch('element.before')
  private onChangeElementBefore(value?: [string, Element]) {
    this.onChangeElement('before', value)
  }

  @Watch('element.after')
  private onChangeElementAfter(value?: [string, Element]) {
    this.onChangeElement('after', value)
  }

  private onChangeElement(diff: Diff, value?: [string, Element]) {
    if (!this.draft) return
    if (value !== undefined) {
      const type = value[1].type
      if (type === 'Statements') {
        this.$refs.editor.setupStatementsCursor(diff)
        this.$refs.editor.hideWidgets(diff)
      } else {
        this.$refs.editor.showWidgets(diff, type)
        this.$refs.editor.disposeStatementsCursor(diff)
      }
    } else {
      this.$refs.editor.hideWidgets(diff)
      this.$refs.editor.disposeStatementsCursor(diff)
    }
  }

  private head() {
    return { title: this.draft ? this.draft.type.name : 'Draft' }
  }
}
</script>

<style lang="scss" scope>
.v-expansion-panels {
  border-radius: 0;
}
.v-expansion-panel.v-expansion-panel--active.v-item--active {
  border-radius: 0;
}
.v-expansion-panel-content__wrap {
  padding: 0;
}
</style>
