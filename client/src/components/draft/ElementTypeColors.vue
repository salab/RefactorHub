<script setup lang="ts">
const elementTypes = computed(() => useDraft().elementTypes.value)
const getTypeColor = (type: string, alpha = 1.0) => {
  const length = elementTypes.value.length
  const index = elementTypes.value.indexOf(type)
  return `hsla(${(index * 360) / length}, 100%, 60%, ${alpha})`
}
// const getCommonTokensColor = (level: number, alpha = 1.0) => {
//   // blue
//   const max = 255
//   const min = 50
//   const value = min + (max - min) / level
//   return `rgba(0,0,${value},${alpha})`
// }
const getColorfulCommonTokensColor = (
  level: number,
  colorType: number,
  alpha = 1.0,
) => {
  const hue = 20 + (360 / 10) * colorType
  const saturation = 100
  const lightness = 80 - 30 / level
  return `hsla(${hue}, ${saturation}%, ${lightness}%, ${alpha})`
}
const getCommonTokensHoveredColor = (level: number, alpha = 1.0) => {
  // red
  const max = 255
  const min = 150
  const value = min + (max - min) / level
  return `rgba(${value},0,0,${alpha})`
}
</script>

<template>
  <component :is="'style'">
    <!-- prettier-ignore -->
    <template v-for="type in elementTypes">
      .element-holder-{{ type }} {
        border-color: {{ getTypeColor(type) }} !important;
      }
      .element-value-{{ type }} {
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
    <!-- prettier-ignore -->
    <template v-for="level in [1, 2]">
      <template v-for="colorType in [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]">
        .commonTokens-decoration-{{ level }}-{{ colorType }} {
          border: {{ 2.5 / (level * level) }}px solid;
          border-color: {{ getColorfulCommonTokensColor(level, colorType, 0.7) }} !important;
        }
        .commonTokens-decoration-hovered-{{ level }} {
          border: {{ 2.5 / (level * level) + 2 }}px solid;
          border-color: {{ getCommonTokensHoveredColor(level, 0.7) }} !important;
        }
      </template>
    </template>
  </component>
</template>
