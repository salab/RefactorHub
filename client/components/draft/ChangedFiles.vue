<template>
  <v-expansion-panels focusable tile :value="0">
    <v-expansion-panel>
      <v-expansion-panel-header class="files-panel-header d-flex pa-0 pr-2">
        <div class="pl-3 mr-6">
          <span
            v-if="files.length > 0 && before !== undefined"
            class="subtitle-1"
            :title="files[before].previousName"
          >
            {{ triming(files[before].previousName) }}
          </span>
        </div>
        <v-divider vertical />
        <div class="pl-3">
          <span
            v-if="files.length > 0 && after !== undefined"
            class="subtitle-1"
            :title="files[after].name"
          >
            {{ triming(files[after].name) }}
          </span>
        </div>
      </v-expansion-panel-header>
      <v-expansion-panel-content>
        <div class="file-list d-flex">
          <div class="flex-grow-1">
            <v-list dense>
              <v-list-item-group v-model="before">
                <v-divider />
                <template v-for="(file, i) in files">
                  <v-list-item
                    :key="i"
                    :disabled="file.status === 'added' || i === before"
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
                  <v-divider :key="`before-divider-${i}`" />
                </template>
              </v-list-item-group>
            </v-list>
          </div>
          <div>
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
                      fa-pen-square
                    </v-icon>
                    <v-icon v-if="file.status === 'added'" small color="green">
                      fa-plus-square
                    </v-icon>
                    <v-icon v-if="file.status === 'removed'" small color="red">
                      fa-minus-square
                    </v-icon>
                    <v-icon
                      v-if="file.status === 'renamed'"
                      small
                      color="purple"
                    >
                      fa-caret-square-right
                    </v-icon>
                  </v-list-item>
                  <v-divider :key="`common-divider-${i}`" />
                </template>
              </v-list-item-group>
            </v-list>
          </div>
          <div class="flex-grow-1">
            <v-list dense>
              <v-list-item-group v-model="after">
                <v-divider />
                <template v-for="(file, i) in files">
                  <v-list-item
                    :key="i"
                    :selectable="false"
                    :disabled="file.status === 'removed' || i === after"
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
                  <v-divider :key="`after-divider-${i}`" />
                </template>
              </v-list-item-group>
            </v-list>
          </div>
        </div>
      </v-expansion-panel-content>
    </v-expansion-panel>
  </v-expansion-panels>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'nuxt-property-decorator'

@Component
export default class ChangedFiles extends Vue {
  private before?: number = 0
  private after?: number = 0
  private common?: number = 0

  @Watch('before')
  private onChangeBefore(value?: number) {
    this.$accessor.draft.setFile({ diff: 'before', value })
  }

  @Watch('after')
  private onChangeAfter(value?: number) {
    this.$accessor.draft.setFile({ diff: 'after', value })
  }

  @Watch('common')
  private onChangeCommon(value?: number) {
    this.before = value
    this.after = value
  }

  private get commit() {
    return this.$accessor.draft.commit
  }

  private get file() {
    return this.$accessor.draft.file
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
}
.file-list {
  max-height: 200px;
  overflow: scroll;
}
</style>
