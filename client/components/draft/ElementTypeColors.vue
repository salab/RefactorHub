<template>
  <component :is="'style'">
    <!-- prettier-ignore -->
    <template v-for="type in elementTypes">
      .element-widget-{{ type }} {
        background-color: {{ getTypeColor(type, 0.2) }};
        color: {{ getTypeColor(type, 0.9) }};
      }
      .element-item-{{ type }} {
        border-color: {{ getTypeColor(type) }} !important;
      }
      .element-decoration-{{ type }} {
        background-color: {{ getTypeColor(type, 0.2) }};
        border-color: {{ getTypeColor(type, 0.9) }} !important;
      }
    </template>
  </component>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator'

@Component
export default class ElementTypeColors extends Vue {
  private get elementTypes() {
    return this.$accessor.draft.elementTypes
  }

  private getTypeColor(type: string, alpha = 1.0) {
    const types = this.elementTypes
    const length = types.length
    const index = types.indexOf(type)
    return `hsla(${(index * 360) / length}, 100%, 60%, ${alpha})`
  }
}
</script>
