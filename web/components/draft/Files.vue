<template>
  <v-expansion-panels focusable>
    <v-expansion-panel>
      <v-expansion-panel-header class="files-panel-header">
        <v-layout>
          <v-flex xs6 pl-3 py-1 mr-8>
            <span
              v-if="files.length > 0 && value.before !== undefined"
              class="subtitle-1"
            >
              {{ triming(files[value.before].previousName) }}
            </span>
          </v-flex>
          <v-divider vertical />
          <v-flex xs6 pl-3 py-1>
            <span
              v-if="files.length > 0 && value.after !== undefined"
              class="subtitle-1"
            >
              {{ triming(files[value.after].name) }}
            </span>
          </v-flex>
        </v-layout>
      </v-expansion-panel-header>
      <v-expansion-panel-content>
        <v-layout>
          <v-flex xs6>
            <v-list dense class="file-list">
              <v-list-item-group v-model="value.before">
                <v-list-item
                  v-for="(file, i) in files"
                  :key="i"
                  :disabled="file.status === 'added' || i === value.before"
                >
                  <v-list-item-content>
                    <v-list-item-title v-if="file.status !== 'added'">{{
                      triming(file.previousName, 70)
                    }}</v-list-item-title>
                  </v-list-item-content>
                </v-list-item>
              </v-list-item-group>
            </v-list>
          </v-flex>
          <v-divider vertical />
          <v-flex xs6>
            <v-list dense class="file-list">
              <v-list-item-group v-model="value.after">
                <v-list-item
                  v-for="(file, i) in files"
                  :key="i"
                  :disabled="file.status === 'removed' || i === value.after"
                >
                  <v-list-item-content>
                    <v-list-item-title v-if="file.status !== 'removed'">{{
                      triming(file.name, 70)
                    }}</v-list-item-title>
                  </v-list-item-content>
                </v-list-item>
              </v-list-item-group>
            </v-list>
          </v-flex>
        </v-layout>
      </v-expansion-panel-content>
    </v-expansion-panel>
  </v-expansion-panels>
</template>

<script lang="ts">
import { Component, Vue, State, Prop } from 'nuxt-property-decorator'
import { CommitInfo } from '~/types'

@Component
export default class Files extends Vue {
  @State('commit') private commit?: CommitInfo
  @Prop({ required: true })
  private value!: { before?: number; after?: number }

  private get files() {
    return this.commit ? this.commit.files : []
  }

  private triming(str: string, length: number = 50): string {
    if (str.length > length) {
      return '...' + str.substr(str.length - length + 3)
    }
    return str
  }
}
</script>

<style lang="scss" scope>
.files-panel-header {
  min-height: 36px !important;
  padding: 0;
  padding-right: 8px;
}
.file-list {
  max-height: 200px;
  overflow: scroll;
}
</style>