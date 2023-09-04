import { DiffCategory } from 'refactorhub'
import { Range } from 'monaco-editor'
import cryptoRandomString from 'crypto-random-string'
import { CommitDetail } from 'apis'

interface ViewerBase {
  id: string
}
export interface FileViewer extends ViewerBase {
  type: 'file'
  category: DiffCategory
  path: string
  range?: Range
}
export interface DiffViewer extends ViewerBase {
  type: 'diff'
  beforePath: string
  afterPath: string
  navigation?: {
    category: DiffCategory
    range: Range
  }
}
export type Viewer = FileViewer | DiffViewer

export interface Destination {
  category: DiffCategory
  path: string
  range: Range
}
export interface Navigator {
  label: string
  currentDestinationIndex: number
  destinations: Destination[]
}

export const useViewer = () => {
  const viewers = useState<Viewer[]>('viewers', () => [])
  const mainViewerId = useState<string>('mainViewerId', () => '')

  const viewerNavigationMap = useState<Map<string, Navigator>>(
    'viewerNavigationMap',
    () => new Map(),
  )

  function initialize() {
    viewers.value = []
    mainViewerId.value = ''
    viewerNavigationMap.value.clear()
  }

  function setup(commit: CommitDetail) {
    useLoader().setLoadingText('setting up viewer')
    const file = commit.files[0]
    const id = cryptoRandomString({ length: 10 })
    viewers.value.push({
      id,
      type: 'diff',
      beforePath: file.previousName,
      afterPath: file.name,
    })
    mainViewerId.value = id
  }

  function createViewer(
    viewer: Omit<FileViewer, 'id'> | Omit<DiffViewer, 'id'>,
    direction: 'next' | 'prev',
  ) {
    const mainIndex = viewers.value.findIndex(
      (viewer) => viewer.id === mainViewerId.value,
    )
    if (mainIndex === -1) {
      throw new Error(`cannot find main viewer: id=${mainViewerId.value}`)
    }

    const id = cryptoRandomString({ length: 10 })
    const index = mainIndex + (direction === 'next' ? 1 : 0)
    mainViewerId.value = id
    viewers.value.splice(index, 0, { ...viewer, id })
    return viewers.value[index]
  }
  function recreateViewer(
    id: string,
    viewer: Omit<FileViewer, 'id'> | Omit<DiffViewer, 'id'>,
  ) {
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
    const navigator = viewerNavigationMap.value.get(id)
    if (navigator) {
      viewerNavigationMap.value.delete(id)
      viewerNavigationMap.value.set(newId, navigator)
    }
    return viewers.value[index]
  }
  function duplicateViewer(id: string) {
    const index = viewers.value.findIndex((viewer) => viewer.id === id)
    if (index === -1) {
      logger.warn(`cannot find viewer: id=${id}`)
      return
    }
    const newId = cryptoRandomString({ length: 10 })
    mainViewerId.value = newId
    viewers.value.splice(index + 1, 0, { ...viewers.value[index], id: newId })
  }
  function deleteViewer(id: string) {
    if (viewers.value.length <= 1) return
    const index = viewers.value.findIndex((viewer) => viewer.id === id)
    if (index === -1) {
      logger.warn(`cannot find viewer: id=${id}`)
      return
    }
    mainViewerId.value = viewers.value[index ? index - 1 : 1].id
    viewers.value.splice(index, 1)
    viewerNavigationMap.value.delete(id)
  }

  function setNavigator(navigator: Navigator, viewerId = mainViewerId.value) {
    viewerNavigationMap.value.set(viewerId, navigator)
  }
  function getNavigator(viewerId: string) {
    return computed(() => viewerNavigationMap.value.get(viewerId))
  }
  function deleteNavigator(viewerId: string) {
    viewerNavigationMap.value.delete(viewerId)
  }
  function navigate(viewerId: string, direction: 'next' | 'prev') {
    const navigator = viewerNavigationMap.value.get(viewerId)
    const viewer = viewers.value.find((viewer) => viewer.id === viewerId)
    if (!navigator || !viewer) {
      logger.warn(`cannot find navigator or viewer: id=${viewerId}`)
      return
    }
    const destinationIndex =
      (navigator.currentDestinationIndex +
        navigator.destinations.length +
        (direction === 'next' ? 1 : -1)) %
      navigator.destinations.length
    const destination = navigator.destinations[destinationIndex]
    const file = useDraft().commit.value?.files.find((file) =>
      destination.category === 'before'
        ? file.previousName === destination.path
        : file.name === destination.path,
    )
    if (!file) {
      logger.warn(`cannot find file ${destination.path}`)
      return
    }
    const newViewer: Viewer =
      viewer.type === 'file'
        ? {
            ...viewer,
            type: 'file',
            ...destination,
          }
        : {
            ...viewer,
            type: 'diff',
            beforePath: file.previousName,
            afterPath: file.name,
            navigation: { ...destination },
          }
    navigator.currentDestinationIndex = destinationIndex
    viewerNavigationMap.value.set(viewerId, navigator)
    recreateViewer(viewerId, newViewer)
  }

  return {
    viewers: computed(() => viewers.value),
    mainViewerId,
    initialize,
    setup,
    createViewer,
    recreateViewer,
    duplicateViewer,
    deleteViewer,
    setNavigator,
    getNavigator,
    deleteNavigator,
    navigate,
  }
}
