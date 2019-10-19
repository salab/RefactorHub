<template>
  <v-layout column fill-height>
    <v-flex>
      <info />
    </v-flex>
    <v-flex xs12>
      <v-layout fill-height>
        <v-flex>
          <elements title="Before" />
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
          <elements title="After" :right="true" />
        </v-flex>
      </v-layout>
    </v-flex>
  </v-layout>
</template>

<script lang="ts">
import { Component, Vue, State, Watch } from 'nuxt-property-decorator'
import { Dispatcher } from 'vuex-type-helper'
import { Draft, CommitInfo } from 'refactorhub'
import Elements from '~/components/draft/Elements.vue'
import Files from '~/components/draft/Files.vue'
import Info from '~/components/draft/Info.vue'
import MonacoEditor from '~/components/draft/MonacoEditor.vue'
import { DraftActions } from '~/store'

@Component({
  components: {
    Elements,
    Files,
    Info,
    MonacoEditor
  }
})
export default class extends Vue {
  @State('draft') private draft?: Draft
  @State('commit') private commit?: CommitInfo
  private file: { before?: number; after?: number } = {}
  private editor?: MonacoEditor

  private async mounted() {
    await Promise.all([
      this.$store.dispatch<Dispatcher<DraftActions>>({
        type: 'fetchDraft',
        id: this.$route.params.id
      }),
      this.$store.dispatch<Dispatcher<DraftActions>>({
        type: 'fetchRefactoringTypes'
      })
    ])
    this.editor = this.$refs.editor as MonacoEditor
    this.file = { before: 0, after: 0 }
  }

  @Watch('file.before')
  private async onChangeFileBefore(value?: number, _?: number) {
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
  private async onChangeFileAfter(value?: number, _?: number) {
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

  private head() {
    return { title: 'Draft' }
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
