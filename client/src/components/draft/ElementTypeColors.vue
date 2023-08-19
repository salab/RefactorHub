<script setup lang="ts">
const elementTypes = computed(() => useDraft().elementTypes.value)
const getTypeColor = (type: string, alpha = 1.0) => {
  const length = elementTypes.value.length
  const index = elementTypes.value.indexOf(type)
  return `hsla(${(index * 360) / length}, 100%, 60%, ${alpha})`
}

const { maxId, getWithId } = useCommonTokenSequence()
function getCommonTokenSequenceBorderWidth(id: number) {
  const { type, isHovered } = getWithId(id).tokenSequenceSet
  let width: number
  switch (type) {
    case 'oneToOne':
      width = 2.2
      break
    case 'oneToManyOrManyToOne':
      width = 1.7
      break
    case 'manyToMany':
      width = 1.3
      break
  }
  if (isHovered) width += 2
  return width
}
function calcHue(id: number) {
  let hue = 0
  let remain = id
  let count = 0
  while (remain !== 0) {
    const flag = remain % 2 === 1
    if (flag) hue += 180 / Math.pow(2, count)
    remain = Math.floor(remain / 2)
    count++
  }
  return hue
}
function getCommonTokenSequenceColor(id: number, alpha = 1.0) {
  const { type, isHovered } = getWithId(id).tokenSequenceSet
  const hue = isHovered ? 0 : calcHue(id)
  const saturation = 100
  let lightness
  switch (type) {
    case 'oneToOne':
      lightness = 50
      break
    case 'oneToManyOrManyToOne':
      lightness = 60
      break
    case 'manyToMany':
      lightness = 70
      break
  }
  return `hsla(${hue}, ${saturation}%, ${lightness}%, ${alpha})`
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
    <!-- prettier-ignore -->
    <template v-for="id in [...new Array(maxId + 1).keys()]">
      .commonTokenSequence-decoration-{{ id }} {
        border: {{ getCommonTokenSequenceBorderWidth(id) }}px solid;
        border-color: {{ getCommonTokenSequenceColor(id) }} !important;
      }
    </template>
  </component>
</template>
