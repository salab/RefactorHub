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
import Elements from '~/components/draft/Elements.vue'
import Files from '~/components/draft/Files.vue'
import Info from '~/components/draft/Info.vue'
import MonacoEditor from '~/components/draft/MonacoEditor.vue'
import { Draft, CommitInfo } from '~/types'
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
    if (!this.editor || !this.commit) return
    const promises: Promise<void>[] = []
    const owner = this.commit.owner
    const repository = this.commit.repository
    if (oldValue.before !== newValue.before && newValue.before !== undefined) {
      const sha = this.commit.parent
      const path = this.commit.files[newValue.before].previousName
      promises.push(
        (async () => {
          if (this.editor) {
            this.editor.setTextModel(
              'original',
              await this.editor.getTextModel(owner, repository, sha, path)
            )
          }
        })()
      )
    }
    if (oldValue.after !== newValue.after && newValue.after !== undefined) {
      const sha = this.commit.sha
      const path = this.commit.files[newValue.after].name
      promises.push(
        (async () => {
          if (this.editor) {
            this.editor.setTextModel(
              'modified',
              await this.editor.getTextModel(owner, repository, sha, path)
            )
          }
        })()
      )
    }
    this.editor.isLoading = true
    await Promise.all(promises)
    this.editor.isLoading = false
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
