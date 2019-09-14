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
              <files />
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
import { Component, Vue, State } from 'nuxt-property-decorator'
import { Dispatcher } from 'vuex-type-helper'
import Elements from '~/components/draft/Elements.vue'
import Files from '~/components/draft/Files.vue'
import Info from '~/components/draft/Info.vue'
import MonacoEditor from '~/components/draft/MonacoEditor.vue'
import { Draft } from '~/types'
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

  private mounted() {
    this.$store.dispatch<Dispatcher<DraftActions>>({
      type: 'fetchDraft',
      id: this.$route.params.id
    })
  }

  private head() {
    return { title: 'Draft' }
  }
}
</script>

<style lang="scss" scope>
.v-expansion-panel.v-expansion-panel--active.v-item--active {
  border-radius: 0;
}
.v-expansion-panel-content__wrap {
  padding: 0;
}
</style>
