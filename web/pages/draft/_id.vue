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
import * as monaco from 'monaco-editor'
import Elements from '~/components/draft/Elements.vue'
import Files from '~/components/draft/Files.vue'
import Info from '~/components/draft/Info.vue'
import MonacoEditor from '~/components/draft/MonacoEditor.vue'
import { Draft, CommitInfo, TextModel } from '~/types'
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

  @Watch('file')
  private async onChangeFile(
    newValue: { before?: number; after?: number },
    oldValue: { before?: number; after?: number }
  ) {
    if (!this.editor) return
    const promises: Promise<void>[] = []
    if (oldValue.before !== newValue.before) {
      promises.push(
        (async () => {
          if (!this.commit || newValue.before === undefined) return
          const model = await this.getTextModel(
            this.commit.owner,
            this.commit.repository,
            this.commit.parent,
            this.commit.files[newValue.before].previousName
          )
          await this.setTextModel('original', model)
        })()
      )
    }
    if (oldValue.after !== newValue.after) {
      promises.push(
        (async () => {
          if (!this.commit || newValue.after === undefined) return
          const model = await this.getTextModel(
            this.commit.owner,
            this.commit.repository,
            this.commit.sha,
            this.commit.files[newValue.after].name
          )
          await this.setTextModel('modified', model)
        })()
      )
    }
    this.editor.isLoading = true
    await Promise.all(promises)
    this.editor.isLoading = false
  }

  private async getTextModel(
    owner: string,
    repository: string,
    sha: string,
    path: string
  ) {
    return (await this.$axios.get<TextModel>(`/api/editor/text_model`, {
      params: { owner, repository, sha, path }
    })).data
  }

  private setTextModel(which: 'original' | 'modified', model: TextModel) {
    if (!this.editor) return
    const newModel = monaco.editor.createModel(
      model.value,
      model.language,
      model.uri ? monaco.Uri.parse(model.uri) : undefined
    )
    if (which === 'original') {
      this.editor.diffEditor.setModel({
        original: newModel,
        modified:
          this.editor.diffEditor.getModifiedEditor().getModel() ||
          monaco.editor.createModel('', 'text/plain')
      })
    } else if (which === 'modified') {
      this.editor.diffEditor.setModel({
        original:
          this.editor.diffEditor.getOriginalEditor().getModel() ||
          monaco.editor.createModel('', 'text/plain'),
        modified: newModel
      })
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
