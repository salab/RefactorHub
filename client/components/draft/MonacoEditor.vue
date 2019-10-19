<template>
  <div id="container" class="fill-height">
    <loading :active.sync="isLoading" :is-full-page="false" />
  </div>
</template>

<script lang="ts">
import { Vue, Component } from 'nuxt-property-decorator'
import * as monaco from 'monaco-editor'
import Loading from 'vue-loading-overlay'
import 'vue-loading-overlay/dist/vue-loading.css'
import { TextModel } from 'refactorhub'

@Component({
  components: {
    Loading
  }
})
export default class MonacoEditor extends Vue {
  public diffEditor!: monaco.editor.IStandaloneDiffEditor
  private pending = 0

  private mounted() {
    const container = document.getElementById('container')
    if (container !== null) {
      this.diffEditor = monaco.editor.createDiffEditor(container, {
        enableSplitViewResizing: false,
        automaticLayout: true,
        readOnly: true,
        theme: 'vs-dark'
      })
    }
  }

  public get isLoading() {
    return this.pending > 0
  }

  public async setTextModel(
    which: 'original' | 'modified',
    owner: string,
    repository: string,
    sha: string,
    path: string
  ) {
    this.pending++

    const uri = this.getUri(owner, repository, sha, path)
    let model = monaco.editor.getModel(uri)
    if (!model) {
      const text = await this.getTextModel(owner, repository, sha, path)
      model = monaco.editor.createModel(text.value, text.language, uri)
    }

    if (which === 'original') {
      this.diffEditor.setModel({
        original: model,
        modified:
          this.diffEditor.getModifiedEditor().getModel() ||
          monaco.editor.createModel('', 'text/plain')
      })
    } else if (which === 'modified') {
      this.diffEditor.setModel({
        original:
          this.diffEditor.getOriginalEditor().getModel() ||
          monaco.editor.createModel('', 'text/plain'),
        modified: model
      })
    }

    this.pending--
  }

  public async getTextModel(
    owner: string,
    repository: string,
    sha: string,
    path: string
  ) {
    return (await this.$axios.get<TextModel>(`/api/editor/text_model`, {
      params: { owner, repository, sha, path }
    })).data
  }

  private getUri(owner: string, repository: string, sha: string, path: string) {
    return monaco.Uri.parse(
      `https://github.com/${owner}/${repository}/blob/${sha}/${path}`
    )
  }
}
</script>

<style lang="scss" scoped>
#container {
  position: relative;
}
</style>