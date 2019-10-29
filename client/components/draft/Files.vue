<template>
  <v-expansion-panels focusable>
    <v-expansion-panel>
      <v-expansion-panel-header class="files-panel-header">
        <v-layout>
          <v-flex xs6 pl-3 py-1 mr-8>
            <span
              v-if="files.length > 0 && value.before !== undefined"
              class="subtitle-1"
              :title="files[value.before].previousName"
            >
              {{ triming(files[value.before].previousName) }}
            </span>
          </v-flex>
          <v-divider vertical />
          <v-flex xs6 pl-3 py-1>
            <span
              v-if="files.length > 0 && value.after !== undefined"
              class="subtitle-1"
              :title="files[value.after].name"
            >
              {{ triming(files[value.after].name) }}
            </span>
          </v-flex>
        </v-layout>
      </v-expansion-panel-header>
      <v-expansion-panel-content>
        <v-layout class="file-list">
          <v-flex xs6>
            <v-list dense>
              <v-list-item-group v-model="value.before">
                <v-divider />
                <template v-for="(file, i) in files">
                  <v-list-item
                    :key="i"
                    :disabled="file.status === 'added' || i === value.before"
                  >
                    <v-list-item-content>
                      <v-list-item-title
                        v-if="file.status !== 'added'"
                        :title="file.previousName"
                      >
                        {{ triming(file.previousName, 70) }}
                      </v-list-item-title>
                    </v-list-item-content>
                  </v-list-item>
                  <v-divider :key="`b-div-${i}`" />
                </template>
              </v-list-item-group>
            </v-list>
          </v-flex>
          <v-flex>
            <v-list dense>
              <v-list-item-group v-model="common">
                <v-divider />
                <template v-for="(file, i) in files">
                  <v-list-item :key="i">
                    <v-icon
                      v-if="file.status === 'modified'"
                      small
                      color="amber"
                    >
                      fa-edit
                    </v-icon>
                    <v-icon v-if="file.status === 'added'" small color="green">
                      fa-plus
                    </v-icon>
                    <v-icon v-if="file.status === 'removed'" small color="red">
                      fa-minus
                    </v-icon>
                    <v-icon
                      v-if="file.status === 'renamed'"
                      small
                      color="purple"
                    >
                      fa-arrow-right
                    </v-icon>
                  </v-list-item>
                  <v-divider :key="`c-div-${i}`" />
                </template>
              </v-list-item-group>
            </v-list>
          </v-flex>
          <v-flex xs6>
            <v-list dense>
              <v-list-item-group v-model="value.after">
                <v-divider />
                <template v-for="(file, i) in files">
                  <v-list-item
                    :key="i"
                    :selectable="false"
                    :disabled="file.status === 'removed' || i === value.after"
                  >
                    <v-list-item-content>
                      <v-list-item-title
                        v-if="file.status !== 'removed'"
                        :title="file.name"
                      >
                        {{ triming(file.name, 70) }}
                      </v-list-item-title>
                    </v-list-item-content>
                  </v-list-item>
                  <v-divider :key="`a-div-${i}`" />
                </template>
              </v-list-item-group>
            </v-list>
          </v-flex>
        </v-layout>
      </v-expansion-panel-content>
    </v-expansion-panel>
  </v-expansion-panels>
</template>

<script lang="ts">
import { Component, Vue, Prop, Watch } from 'nuxt-property-decorator'

@Component
export default class Files extends Vue {
  @Prop({ required: true })
  private value!: { before?: number; after?: number }
  private common?: number = 0

  @Watch('common')
  private onChangeCommon(value: number) {
    this.value.before = value
    this.value.after = value
  }

  private get commit() {
    return this.$accessor.draft.commit
  }

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