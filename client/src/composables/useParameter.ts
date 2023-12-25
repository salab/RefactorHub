import { DiffCategory, ElementMetadata } from 'refactorhub'
import { useState } from '#imports'

export const useParameter = () => {
  const hoveredElement = useState<{
    [category in DiffCategory]?: ElementMetadata
  }>('hoveredElement', () => ({
    before: undefined,
    after: undefined,
  }))
  const editingElement = useState<{
    [category in DiffCategory]?: ElementMetadata
  }>('editingElement', () => ({
    before: undefined,
    after: undefined,
  }))

  function initialize() {
    hoveredElement.value.before = undefined
    hoveredElement.value.after = undefined
    editingElement.value.before = undefined
    editingElement.value.after = undefined
  }

  function updateHoveredElement(
    category: DiffCategory,
    element: ElementMetadata | undefined,
  ) {
    hoveredElement.value[category] = element
  }
  function updateEditingElement(
    category: DiffCategory,
    element: ElementMetadata | undefined,
  ) {
    editingElement.value[category] = element
  }

  return {
    hoveredElement: computed(() => hoveredElement.value),
    editingElement: computed(() => editingElement.value),
    initialize,
    updateHoveredElement,
    updateEditingElement,
  }
}
