import { DiffCategory, ElementMetadata } from 'refactorhub'
import {
  showElementWidgetsWithType,
  hideElementWidgets,
} from './elementWidgets'
import {
  setupCodeFragmentsCursor,
  disposeCodeFragmentsCursor,
} from './codeFragments'

export function changeEditingElement(
  category: DiffCategory,
  metadata?: ElementMetadata
) {
  if (metadata !== undefined) {
    if (metadata.type === 'CodeFragments') {
      setupCodeFragmentsCursor(category)
      hideElementWidgets(category)
    } else {
      showElementWidgetsWithType(category, metadata.type)
      disposeCodeFragmentsCursor(category)
    }
  } else {
    hideElementWidgets(category)
    disposeCodeFragmentsCursor(category)
  }
}
