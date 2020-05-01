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
import { defineComponent } from '@vue/composition-api'

export default defineComponent({
  name: 'ElementTypeColors',
  setup(_, { root }) {
    const elementTypes = root.$accessor.draft.elementTypes
    const getTypeColor = (type: string, alpha = 1.0) => {
      const length = elementTypes.length
      const index = elementTypes.indexOf(type)
      return `hsla(${(index * 360) / length}, 100%, 60%, ${alpha})`
    }
    return {
      elementTypes,
      getTypeColor,
    }
  },
})
</script>
