<script setup lang="ts">
const hoveredCommonTokenSequencesHueId = 0
const selectedCommonTokenSequencesHueId = 5
const hoveredElementHueId = 3
const editingElementHueId = 7
const otherCommonTokenSequencesHueIdMin = 8

const { maxId, selectedId, getWithId } = useCommonTokenSequence()

function calcHue(hueId: number) {
  let hue = 0
  let remain = hueId
  let count = 0
  while (remain !== 0) {
    const flag = remain % 2 === 1
    if (flag) hue += 180 / Math.pow(2, count)
    remain = Math.floor(remain / 2)
    count++
  }
  return hue
}
const getHoveredElementColor = (alpha = 1.0) => {
  return `hsla(${calcHue(hoveredElementHueId)}, 100%, 60%, ${alpha})`
}
const getEditingElementColor = (alpha = 1.0) => {
  return `hsla(${calcHue(editingElementHueId)}, 100%, 60%, ${alpha})`
}

function getCommonTokenSequenceColor(id: number, alpha = 1.0) {
  const { type, isHovered } = getWithId(id).tokenSequenceSet
  const hue = isHovered
    ? calcHue(hoveredCommonTokenSequencesHueId)
    : selectedId.value === id
    ? calcHue(selectedCommonTokenSequencesHueId)
    : calcHue(otherCommonTokenSequencesHueIdMin + id)
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
function getCommonTokenSequenceBackgroundColor(id: number) {
  const { type, isHovered } = getWithId(id).tokenSequenceSet
  const hue = isHovered
    ? calcHue(hoveredCommonTokenSequencesHueId)
    : selectedId.value === id
    ? calcHue(selectedCommonTokenSequencesHueId)
    : calcHue(otherCommonTokenSequencesHueIdMin + id)
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
  const alpha = selectedId.value === id ? 0.2 : 0
  return `hsla(${hue}, ${saturation}%, ${lightness}%, ${alpha})`
}
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
  if (selectedId.value === id || isHovered) width += 2
  return width
}
</script>

<template>
  <!-- prettier-ignore -->
  <component :is="'style'">
    .element-value-hovered {
      background-color: {{ getHoveredElementColor(0.2) }};
    }
    .element-value-editing {
      background-color: {{ getEditingElementColor(0.2) }};
    }
    .element-widget {
      cursor: pointer;
      border: 2px solid;
      opacity: 0.6;
      background-color: {{ getEditingElementColor(0.2) }};
      color: {{ getEditingElementColor(0.9) }};
      &:hover {
        opacity: 1;
      }
    }
    .element-decoration {
      border: 1px solid;
      background-color: {{ getHoveredElementColor(0.2) }};
      border-color: {{ getHoveredElementColor(0.9) }} !important;
    }
    <template v-for="id in [...new Array(maxId + 1).keys()]">
      .commonTokenSequence-decoration-{{ id }} {
        border: {{ getCommonTokenSequenceBorderWidth(id) }}px solid;
        background-color: {{ getCommonTokenSequenceBackgroundColor(id) }};
        border-color: {{ getCommonTokenSequenceColor(id) }} !important;
      }
    </template>
  </component>
</template>
