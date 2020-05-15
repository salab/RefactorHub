import { DiffCategory, ElementMetadata } from 'refactorhub'
import {
  showElementWidgetsWithType,
  hideElementWidgets,
} from './elementWidgets'

export function changeEditingElement(
  category: DiffCategory,
  metadata?: ElementMetadata
) {
  if (metadata !== undefined) {
    if (metadata.type === 'CodeFragments') {
      // TODO:
    } else {
      showElementWidgetsWithType(category, metadata.type)
    }
  } else {
    hideElementWidgets(category)
  }
}
