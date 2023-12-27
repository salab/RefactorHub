<script setup lang="ts">
import { DiffCategory } from 'refactorhub'
import { Change } from 'apis'

const props = defineProps({
  currentChange: {
    type: Object as () => Change,
    required: true,
  },
  category: {
    type: String as () => DiffCategory,
    required: true,
  },
})

const { canEditCurrentChange } = useAnnotation()
const isActive = ref(true)

const elementMetadataMap = computed(() => {
  const type = useAnnotation().changeTypes.value.find(
    (t) => t.name === props.currentChange.typeName,
  )
  return type ? type[props.category] : {}
})
const elementHolderMap = computed(() => {
  const entries = Object.entries(
    props.currentChange.parameterData[props.category],
  )
  const map = elementMetadataMap.value
  entries.sort((a, b) => {
    if (a[0] in map && b[0] in map) {
      if (map[a[0]].required && !map[b[0]].required) return -1
      if (!map[a[0]].required && map[b[0]].required) return 1
    } else if (a[0] in map) return -1
    else if (b[0] in map) return 1
    return a[0] < b[0] ? -1 : a[0] > b[0] ? 1 : 0
  })
  return Object.fromEntries(entries)
})
const isRemovable = (key: string) =>
  !Object.keys(elementMetadataMap.value).includes(key)
</script>

<template>
  <v-navigation-drawer
    :model-value="isActive"
    :location="category === 'after' ? 'right' : 'left'"
    :width="225"
    permanent
  >
    <div class="d-flex flex-column fill-height">
      <v-sheet
        :color="
          useAnnotation().isDividingChange.value && props.category === 'after'
            ? '#c9eeff'
            : colors[category]
        "
        class="d-flex justify-center py-1"
      >
        <span class="text-button"
          >{{
            `${
              useAnnotation().isDividingChange.value &&
              props.category === 'after'
                ? 'intermediate'
                : category
            } Parameters`
          }}
        </span>
        <v-tooltip location="top center" origin="auto">
          <template #activator="{ props: tooltipProps }">
            <div
              :class="{
                ['expand-toggle-before-open']:
                  props.category === 'before' && !isActive,
                ['expand-toggle-before-close']:
                  props.category === 'before' && isActive,
                ['expand-toggle-after-open']:
                  props.category === 'after' && !isActive,
                ['expand-toggle-after-close']:
                  props.category === 'after' && isActive,
              }"
            >
              <v-btn
                v-bind="tooltipProps"
                size="30"
                :color="
                  useAnnotation().isDividingChange.value &&
                  props.category === 'after'
                    ? '#c9eeff'
                    : colors[props.category]
                "
                :icon="
                  (props.category === 'before' && isActive) ||
                  (props.category === 'after' && !isActive)
                    ? '$mdiChevronDoubleLeft'
                    : '$mdiChevronDoubleRight'
                "
                @click="isActive = !isActive"
              />
            </div>
          </template>
          {{ isActive ? 'Close Parameter List' : 'Open Parameter List' }}
        </v-tooltip>
      </v-sheet>
      <v-divider />
      <div class="flex-grow-1 list-container">
        <v-list :opened="Object.keys(elementHolderMap)" class="py-0">
          <element-holder
            v-for="(holder, key) in elementHolderMap"
            :key="key"
            :category="category"
            :element-key="key.toString()"
            :element-holder="holder"
            :element-metadata="elementMetadataMap[key]"
            :is-removable="isRemovable(key.toString())"
          />
        </v-list>
      </div>
      <div v-if="canEditCurrentChange">
        <v-divider />
        <element-key-put-button :category="category" />
      </div>
    </div>
  </v-navigation-drawer>
</template>

<style lang="scss" scoped>
.list-container {
  min-height: 0;
  overflow-y: scroll;
}

.v-navigation-drawer--mini-variant,
.v-navigation-drawer {
  overflow: visible !important;
}

.expand-toggle-before-close {
  position: absolute;
  z-index: 1;
  right: -15px;
  top: 30px;
  .v-btn {
    margin: auto;
    border: thin solid white !important;
  }
}
.expand-toggle-before-open {
  position: absolute;
  z-index: 1;
  right: -40px;
  top: 30px;
  .v-btn {
    margin: auto;
    border: thin solid white !important;
  }
}
.expand-toggle-after-close {
  position: absolute;
  z-index: 1;
  left: -15px;
  top: 30px;
  .v-btn {
    margin: auto;
    border: thin solid white !important;
  }
}
.expand-toggle-after-open {
  position: absolute;
  z-index: 1;
  left: -40px;
  top: 30px;
  .v-btn {
    margin: auto;
    border: thin solid white !important;
  }
}
</style>
