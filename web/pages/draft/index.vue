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
import { Component, Vue } from 'nuxt-property-decorator'
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
  private head() {
    return { title: 'Draft' }
  }
}
</script>

<style lang="scss" scope>
.radius-0 {
  border-radius: 0 !important;
}
.min-height-0 {
  min-height: 0 !important;
}
.v-expansion-panel-content__wrap {
  padding: 0;
}
</style>
