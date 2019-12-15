<template>
  <v-layout column fill-height>
    <draft-header v-if="draft" :id="draft.id" />
    <v-flex>
      <commit-info />
    </v-flex>
    <v-flex xs12>
      <v-layout fill-height>
        <v-flex>
          <element-items />
        </v-flex>
        <v-flex xs12>
          <v-layout column fill-height>
            <v-flex xs12>
              <monaco-editor ref="editor" />
            </v-flex>
            <v-flex>
              <changed-files />
            </v-flex>
          </v-layout>
        </v-flex>
        <v-flex>
          <element-items :diff="'after'" />
        </v-flex>
      </v-layout>
    </v-flex>
    <element-type-colors />
  </v-layout>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'nuxt-property-decorator'
import { Diff, Element } from 'refactorhub'
import ChangedFiles from '~/components/draft/ChangedFiles.vue'
import CommitInfo from '~/components/draft/CommitInfo.vue'
import DraftHeader from '~/components/draft/DraftHeader.vue'
import ElementItems from '~/components/draft/ElementItems.vue'
import ElementTypeColors from '~/components/draft/ElementTypeColors.vue'
import MonacoEditor from '~/components/draft/MonacoEditor.vue'

@Component({
  components: {
    ChangedFiles,
    CommitInfo,
    DraftHeader,
    ElementItems,
    ElementTypeColors,
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

  private destroyed() {
    this.$accessor.draft.setFile({ diff: 'before' })
    this.$accessor.draft.setFile({ diff: 'after' })
    this.$accessor.draft.setElement({ diff: 'before' })
    this.$accessor.draft.setElement({ diff: 'after' })
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
            this.$refs.editor.setElementDecoration(diff, key, element)
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
        this.$refs.editor.hideElementWidgets(diff)
      } else {
        this.$refs.editor.showElementWidgets(diff, type)
        this.$refs.editor.disposeStatementsCursor(diff)
      }
    } else {
      this.$refs.editor.hideElementWidgets(diff)
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
