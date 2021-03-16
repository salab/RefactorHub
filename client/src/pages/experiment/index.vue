<template>
  <v-container>
    <div class="py-3">
      <div class="d-flex align-center justify-space-between">
        <h1 class="text-h4">Experiments</h1>
        <v-btn
          depressed
          small
          class="text-none"
          :disabled="state === 'Pending'"
          @click="prepare"
        >
          {{ state }}
        </v-btn>
      </div>
    </div>
    <v-divider />
    <div class="py-2">
      <div>
        <v-card
          v-for="experiment in actives"
          :key="experiment.id"
          outlined
          class="my-4"
          :to="`/experiment/${experiment.id}`"
        >
          <div class="d-flex align-center">
            <div class="flex-grow-1 px-4 py-2">
              <div class="title">
                {{ experiment.title }}
              </div>
              <div>
                <div class="body-2">{{ experiment.description }}</div>
              </div>
            </div>
          </div>
        </v-card>
      </div>
    </div>
    <v-divider />
    <div class="py-3">
      <h2 class="text-h5">Create a new Experiment</h2>
      <v-card outlined class="my-3 pa-4">
        <v-text-field
          v-model="form.title"
          label="title"
          hide-details
          required
          class="mb-4"
        />
        <v-textarea
          v-model="form.description"
          label="description"
          rows="1"
          hide-details
          required
          class="my-4"
        />
        <v-textarea
          v-model="form.refactorings"
          label="refactorings (NDJSON)"
          rows="3"
          hide-details
          required
          class="mt-4 mb-2"
        />
        <details class="mb-4">
          <summary class="text-caption">examples for refactorings</summary>
          <pre
            v-for="example in examples"
            :key="example"
            class="example px-3 py-2 mt-2"
          ><code>{{ example }}</code></pre>
        </details>
        <div>{{ message }}</div>
        <div class="d-flex justify-center">
          <v-btn :disabled="pending" depressed color="primary" @click="create">
            Create
          </v-btn>
        </div>
      </v-card>
    </div>
  </v-container>
</template>

<script lang="ts">
import {
  computed,
  defineComponent,
  reactive,
  ref,
  useAsync,
} from '@nuxtjs/composition-api'
import apis, { Experiment, CreateRefactoringRequest } from '@/apis'

export default defineComponent({
  setup() {
    const experiments = ref<Experiment[]>([])
    const form = reactive({
      title: '',
      description: '',
      refactorings: '',
    })
    const message = ref('')
    const pending = ref(false)
    const examples = [
      [
        `{"type":"Extract Method","commit":{"sha":"cb49e436b9d7ee55f2531ebc2ef1863f5c9ba9fe","owner":"rstudio","repository":"rstudio"},"data":{"before":{},"after":{}},"description":"Extract Method protected setMaxHeight(maxHeight int) : void extracted from protected wrapMenuBar(menuBar ToolbarMenuBar) : Widget in class org.rstudio.core.client.widget.ScrollableToolbarPopupMenu"}`,
        `{"type":"Move Attribute","commit":{"sha":"f05e86c4d31987ff2f30330745c3eb605de4c4dc","owner":"Graylog2","repository":"graylog2-server"},"data":{"before":{},"after":{}},"description":"Move Attribute private COMPARATOR : Comparator<IndexRange> from class org.graylog2.indexer.ranges.MongoIndexRangeService to public COMPARATOR : Comparator<IndexRange> from class org.graylog2.indexer.ranges.IndexRange"}`,
        `{"type":"Any","commit":{"sha":"08f37df9f39f101bba0ee96845e232d2c72bf426","owner":"JetBrains","repository":"intellij-community"},"data":{"before":{},"after":{}},"description":""}`,
      ].join('\n'),
      [
        `{"type":"Extract Method","commit":{"sha":"6cf596df183b3c3a38ed5dd9bb3b0100c6548ebb","owner":"realm","repository":"realm-java"},"data":{"before":{"target method":{"type":"MethodDeclaration","multiple":false,"elements":[{"name":"showStatus","className":"io.realm.examples.realmmigrationexample.MigrationExampleActivity","location":{"path":"examples/migrationExample/src/main/java/io/realm/examples/realmmigrationexample/MigrationExampleActivity.java","range":{"startLine":112,"startColumn":5,"endLine":118,"endColumn":6}},"type":"MethodDeclaration"}]},"extracted code":{"type":"CodeFragment","multiple":true,"elements":[{"methodName":null,"className":null,"location":{"path":"examples/migrationExample/src/main/java/io/realm/examples/realmmigrationexample/MigrationExampleActivity.java","range":{"startLine":114,"startColumn":9,"endLine":114,"endColumn":25}},"type":"CodeFragment"},{"methodName":null,"className":null,"location":{"path":"examples/migrationExample/src/main/java/io/realm/examples/realmmigrationexample/MigrationExampleActivity.java","range":{"startLine":115,"startColumn":9,"endLine":115,"endColumn":42}},"type":"CodeFragment"},{"methodName":null,"className":null,"location":{"path":"examples/migrationExample/src/main/java/io/realm/examples/realmmigrationexample/MigrationExampleActivity.java","range":{"startLine":116,"startColumn":9,"endLine":116,"endColumn":25}},"type":"CodeFragment"},{"methodName":null,"className":null,"location":{"path":"examples/migrationExample/src/main/java/io/realm/examples/realmmigrationexample/MigrationExampleActivity.java","range":{"startLine":117,"startColumn":9,"endLine":117,"endColumn":32}},"type":"CodeFragment"}]}},"after":{"target method":{"type":"MethodDeclaration","multiple":false,"elements":[{"name":"showStatus","className":"io.realm.examples.realmmigrationexample.MigrationExampleActivity","location":{"path":"examples/migrationExample/src/main/java/io/realm/examples/realmmigrationexample/MigrationExampleActivity.java","range":{"startLine":127,"startColumn":5,"endLine":129,"endColumn":6}},"type":"MethodDeclaration"}]},"extracted method":{"type":"MethodDeclaration","multiple":false,"elements":[{"name":"showStatus","className":"io.realm.examples.realmmigrationexample.MigrationExampleActivity","location":{"path":"examples/migrationExample/src/main/java/io/realm/examples/realmmigrationexample/MigrationExampleActivity.java","range":{"startLine":131,"startColumn":5,"endLine":136,"endColumn":6}},"type":"MethodDeclaration"}]},"invocation":{"type":"MethodInvocation","multiple":false,"elements":[{"methodName":"showStatus","className":null,"location":{"path":"examples/migrationExample/src/main/java/io/realm/examples/realmmigrationexample/MigrationExampleActivity.java","range":{"startLine":128,"startColumn":9,"endLine":128,"endColumn":39}},"type":"MethodInvocation"}]},"extracted code":{"type":"CodeFragment","multiple":true,"elements":[{"methodName":null,"className":null,"location":{"path":"examples/migrationExample/src/main/java/io/realm/examples/realmmigrationexample/MigrationExampleActivity.java","range":{"startLine":132,"startColumn":9,"endLine":132,"endColumn":25}},"type":"CodeFragment"},{"methodName":null,"className":null,"location":{"path":"examples/migrationExample/src/main/java/io/realm/examples/realmmigrationexample/MigrationExampleActivity.java","range":{"startLine":133,"startColumn":9,"endLine":133,"endColumn":42}},"type":"CodeFragment"},{"methodName":null,"className":null,"location":{"path":"examples/migrationExample/src/main/java/io/realm/examples/realmmigrationexample/MigrationExampleActivity.java","range":{"startLine":134,"startColumn":9,"endLine":134,"endColumn":25}},"type":"CodeFragment"},{"methodName":null,"className":null,"location":{"path":"examples/migrationExample/src/main/java/io/realm/examples/realmmigrationexample/MigrationExampleActivity.java","range":{"startLine":135,"startColumn":9,"endLine":135,"endColumn":32}},"type":"CodeFragment"}]}}},"description":"Extract Method\\tprivate showStatus(txt String) : void extracted from private showStatus(realm Realm) : void in class io.realm.examples.realmmigrationexample.MigrationExampleActivity"}`,
        `{"type":"Move Attribute","commit":{"sha":"6abc40ed4850d74ee6c155f5a28f8b34881a0284","owner":"BuildCraft","repository":"BuildCraft"},"data":{"before":{"target field":{"type":"FieldDeclaration","multiple":false,"elements":[{"name":"BUTTON_TEXTURES","className":"buildcraft.core.lib.gui.buttons.GuiBetterButton","location":{"path":"common/buildcraft/core/lib/gui/buttons/GuiBetterButton.java","range":{"startLine":26,"startColumn":39,"endLine":26,"endColumn":120}},"type":"FieldDeclaration"}]}},"after":{"moved field":{"type":"FieldDeclaration","multiple":false,"elements":[{"name":"BUTTON_TEXTURES","className":"buildcraft.core.lib.gui.buttons.StandardButtonTextureSets","location":{"path":"common/buildcraft/core/lib/gui/buttons/StandardButtonTextureSets.java","range":{"startLine":18,"startColumn":39,"endLine":18,"endColumn":120}},"type":"FieldDeclaration"}]}}},"description":"Move Attribute\\tpublic BUTTON_TEXTURES : ResourceLocation from class buildcraft.core.lib.gui.buttons.GuiBetterButton to public BUTTON_TEXTURES : ResourceLocation from class buildcraft.core.lib.gui.buttons.StandardButtonTextureSets"}`,
      ].join('\n'),
    ]

    useAsync(async () => {
      try {
        experiments.value = (await apis.experiments.getAllExperiments()).data
      } catch {}
    })

    const actives = computed(() => experiments.value.filter((e) => e.isActive))

    const state = ref<'Prepare' | 'Pending' | 'Completed'>('Prepare')
    const prepare = async () => {
      state.value = 'Pending'
      const res = await apis.annotator.prepareCommitContents()
      if (res) state.value = 'Completed'
    }

    const create = async () => {
      if (form.title === '' || form.refactorings === '') return
      pending.value = true
      try {
        const refactorings: CreateRefactoringRequest[] = form.refactorings
          .trim()
          .split('\n')
          .map((it) => JSON.parse(it))
        const experiment = (
          await apis.experiments.createExperiment({
            title: form.title,
            description: form.description,
            refactorings,
          })
        ).data
        experiments.value.push(experiment)
        form.title = ''
        form.description = ''
        form.refactorings = ''
        message.value = `Experiment added: ${experiment.title}`
      } catch (e) {
        message.value = `Failed: ${e}`
      } finally {
        pending.value = false
      }
    }

    return {
      experiments,
      actives,
      state,
      prepare,
      form,
      message,
      pending,
      examples,
      create,
    }
  },
})
</script>

<style lang="scss" scoped>
.example {
  overflow-x: auto;
  background: whitesmoke;
  code {
    padding: 0;
    background: transparent;
  }
}
</style>
