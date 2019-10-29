<template>
  <v-layout column fill-height>
    <v-flex>
      <info />
    </v-flex>
    <v-flex xs12>
      <v-layout fill-height>
        <v-flex>
          <elements v-model="element" />
        </v-flex>
        <v-flex xs12>
          <v-layout column fill-height>
            <v-flex xs12>
              <monaco-editor ref="editor" />
            </v-flex>
            <v-flex>
              <files v-model="file" />
            </v-flex>
          </v-layout>
        </v-flex>
        <v-flex>
          <elements v-model="element" :diff="'modified'" />
        </v-flex>
      </v-layout>
    </v-flex>
  </v-layout>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'nuxt-property-decorator'
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
  private file: { before?: number; after?: number } = {}
  private element: { before?: number; after?: number } = {
    before: undefined,
    after: undefined
  }
  private editor?: MonacoEditor

  private get draft() {
    return this.$accessor.draft.draft
  }
  private get commit() {
    return this.$accessor.draft.commit
  }

  private async mounted() {
    await Promise.all([
      this.$accessor.draft.fetchDraft(parseInt(this.$route.params.id)),
      this.$accessor.draft.fetchRefactoringTypes(),
      this.$accessor.draft.fetchElementTypes()
    ])
    this.editor = this.$refs.editor as MonacoEditor
    this.file = { before: 0, after: 0 }
  }

  @Watch('file.before')
  private async onChangeFileBefore(value?: number) {
    if (this.editor && this.commit && value !== undefined) {
      await this.editor.setTextModel(
        'original',
        this.commit.owner,
        this.commit.repository,
        this.commit.parent,
        this.commit.files[value].previousName
      )
    }
  }

  @Watch('file.after')
  private async onChangeFileAfter(value?: number) {
    if (this.editor && this.commit && value !== undefined) {
      await this.editor.setTextModel(
        'modified',
        this.commit.owner,
        this.commit.repository,
        this.commit.sha,
        this.commit.files[value].name
      )
    }
  }

  @Watch('element.before')
  private onChangeElementBefore(value?: number) {
    if (!this.draft || !this.editor) return
    if (value !== undefined) {
      this.editor.showWidgets(
        'original',
        Object.values(this.draft.data.before)[value].type
      )
    } else {
      this.editor.showWidgets('original', '')
    }
  }

  @Watch('element.after')
  private onChangeElementAfter(value?: number) {
    if (!this.draft || !this.editor) return
    if (value !== undefined) {
      this.editor.showWidgets(
        'modified',
        Object.values(this.draft.data.after)[value].type
      )
    } else {
      this.editor.showWidgets('modified', '')
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
