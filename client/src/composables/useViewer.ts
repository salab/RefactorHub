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
  }
  return {
    viewers: computed(() => viewers),
    init,
  }
}
