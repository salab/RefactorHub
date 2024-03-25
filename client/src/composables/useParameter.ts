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
  const autoHighlightedElements = useState<{
    [category in DiffCategory]: ElementMetadata[]
  }>('autoHighlightedElements', () => ({
    before: [],
    after: [],
  }))

  function initialize() {
    hoveredElement.value.before = undefined
    hoveredElement.value.after = undefined
    editingElement.value.before = undefined
    editingElement.value.after = undefined
    autoHighlightedElements.value.before = []
    autoHighlightedElements.value.after = []
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
  function addAutoHighlightedElement(
    category: DiffCategory,
    element: ElementMetadata,
  ) {
    autoHighlightedElements.value[category].push(element)
    setTimeout(() => {
      const index = autoHighlightedElements.value[category].findIndex(
        ({ key, index }) => key === element.key && index === element.index,
      )
      if (index === -1) return
      autoHighlightedElements.value[category].splice(index, 1)
    }, 2000)
  }

  return {
    hoveredElement: computed(() => hoveredElement.value),
    editingElement: computed(() => editingElement.value),
    autoHighlightedElements: computed(() => autoHighlightedElements.value),
    initialize,
    updateHoveredElement,
    updateEditingElement,
    addAutoHighlightedElement,
  }
}
