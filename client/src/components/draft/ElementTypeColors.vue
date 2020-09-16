<template>
  <component :is="'style'">
    <!-- prettier-ignore -->
    <template v-for="type in elementTypes">
      .element-data-{{ type }} {
        border-color: {{ getTypeColor(type) }} !important;
      }
      .element-location-{{ type }} {
        background-color: {{ getTypeColor(type, 0.2) }};
      }
      .element-widget-{{ type }} {
        background-color: {{ getTypeColor(type, 0.2) }};
        color: {{ getTypeColor(type, 0.9) }};
      }
      .element-decoration-{{ type }} {
        background-color: {{ getTypeColor(type, 0.2) }};
        border-color: {{ getTypeColor(type, 0.9) }} !important;
      }
    </template>
  </component>
</template>

<script lang="ts">
import { defineComponent, computed } from '@nuxtjs/composition-api'

export default defineComponent({
  name: 'ElementTypeColors',
  setup(_, { root }) {
    const elementTypes = computed(() => root.$accessor.draft.elementTypes)
    const getTypeColor = (type: string, alpha = 1.0) => {
      const length = elementTypes.value.length
      const index = elementTypes.value.indexOf(type)
      return `hsla(${(index * 360) / length}, 100%, 60%, ${alpha})`
    }
    return {
      elementTypes,
      getTypeColor,
    }
  },
})
</script>
