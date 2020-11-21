import { DiffCategory, ElementMetadata } from 'refactorhub'
import {
  showElementWidgetsWithType,
  hideElementWidgets,
} from './elementWidgets'
import {
  setupCodeFragmentCursor,
  disposeCodeFragmentCursor,
} from './codeFragments'

export function setupEditingElement(
  category: DiffCategory,
  metadata?: ElementMetadata
) {
  if (metadata !== undefined) {
    if (metadata.type === 'CodeFragment') {
      setupCodeFragmentCursor(category)
      hideElementWidgets(category)
    } else {
      showElementWidgetsWithType(category, metadata.type)
      disposeCodeFragmentCursor(category)
    }
  } else {
    hideElementWidgets(category)
    disposeCodeFragmentCursor(category)
  }
}
