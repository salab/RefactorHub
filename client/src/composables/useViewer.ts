import { DiffCategory } from 'refactorhub'
import { Range } from 'monaco-editor'
import cryptoRandomString from 'crypto-random-string'
import { CommitDetail } from 'apis'

export interface Navigation {
  category: DiffCategory
  path: string
  range: Range
}
export interface Navigator {
  label: string
  navigations: Navigation[]
}
interface ViewerBase {
  id: string
  navigator: Navigator
}
export interface FileViewer extends ViewerBase {
  type: 'file'
  category: DiffCategory
  path: string
}
export interface DiffViewer extends ViewerBase {
  type: 'diff'
  beforePath: string
  afterPath: string
}
export type Viewer = FileViewer | DiffViewer

export const useViewer = () => {
  const viewers = useState<Viewer[]>('viewers', () => [])
  const mainViewerId = useState<string>('mainViewerId', () => '')

  function recreateViewer(id: string, viewer: Viewer) {
    const index = viewers.value.findIndex((viewer) => viewer.id === id)
    if (index === -1) {
      logger.warn(`cannot find viewer: id=${id}`)
      return
    }
    // change id in order to change key of element
    const newId = cryptoRandomString({ length: 10 })
    mainViewerId.value = newId
    viewers.value[index] = {
      ...viewer,
      id: newId,
    }
  }

  const init = (commit: CommitDetail) => {
    viewers.value = []
    const file = commit.files[0]
    viewers.value.push({
      id: cryptoRandomString({ length: 10 }),
      type: 'diff',
      beforePath: file.previousName,
      afterPath: file.name,
      navigator: {
        label: '',
        navigations: [],
      },
    })
    mainViewerId.value = viewers.value[0].id
  }

  return {
    viewers: computed(() => viewers),
    mainViewerId,
    init,
    recreateViewer,
  }
}
