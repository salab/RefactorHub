import { DiffCategory } from 'refactorhub'
import { Range } from 'monaco-editor'
import cryptoRandomString from 'crypto-random-string'
import { FilePair } from './useAnnotation'
import { ActionName } from '@/apis'

interface ViewerBase {
  readonly id: string
  /** file pair at current snapshotId */
  readonly filePair: FilePair
  readonly navigation?: {
    readonly category: DiffCategory
    readonly range?: Range
  }
}
export interface FileViewer extends ViewerBase {
  readonly type: 'file'
  readonly navigation: {
    readonly category: DiffCategory
    readonly range?: Range
  }
}
export interface DiffViewer extends ViewerBase {
  readonly type: 'diff'
  readonly navigation?: {
    readonly category: DiffCategory
    readonly range: Range
  }
}
export type Viewer = FileViewer | DiffViewer

export interface Destination {
  readonly category: DiffCategory
  readonly path: string
  readonly range: Range
}
export interface Navigator {
  readonly label: string
  readonly currentDestinationIndex: number
  readonly destinations: Destination[]
}

export const useViewer = () => {
  const viewers = useState<Viewer[]>('viewers', () => [])
  const mainViewerId = useState<string>('mainViewerId', () => '')

  const viewerNavigationMap = useState<Map<string, Navigator>>(
    'viewerNavigationMap',
    () => new Map(),
  )

  function sendViewerAction() {
    sendAction(ActionName.UpdateViewers, {
      viewers: viewers.value.map((viewer) => ({
        id: viewer.id,
        type: viewer.type,
        pathPair: viewer.filePair.getPathPair(),
        filePairStatus: viewer.filePair.status,
        navigation: viewer.navigation,
      })),
    })
  }

  function initialize() {
    viewers.value = []
    mainViewerId.value = ''
    viewerNavigationMap.value.clear()
  }

  function setup(initialFilePair: FilePair) {
    useLoader().setLoadingText('setting up viewer')
    const id = cryptoRandomString({ length: 10 })
    viewers.value.push({
      id,
      type: 'diff',
      filePair: initialFilePair,
    })
    mainViewerId.value = id
    sendViewerAction()
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

    sendViewerAction()
    return viewers.value[index]
  }
  function updateViewer(
    id: string,
    viewer: Omit<FileViewer, 'id'> | Omit<DiffViewer, 'id'>,
  ) {
    const index = viewers.value.findIndex((viewer) => viewer.id === id)
    if (index === -1) {
      logger.warn(`cannot find viewer: id=${id}`)
      return
    }
    viewers.value[index] = {
      ...viewer,
      id,
    }

    sendViewerAction()
    return viewers.value[index]
  }
  function recreateViewer(id: string) {
    const index = viewers.value.findIndex((viewer) => viewer.id === id)
    if (index === -1) {
      logger.warn(`cannot find viewer: id=${id}`)
      return
    }
    // change id in order to change key of element
    const newId = cryptoRandomString({ length: 10 })
    mainViewerId.value = newId
    viewers.value[index] = {
      ...viewers.value[index],
      id: newId,
    }
    const navigator = viewerNavigationMap.value.get(id)
    if (navigator) {
      viewerNavigationMap.value.delete(id)
      viewerNavigationMap.value.set(newId, navigator)
    }

    sendViewerAction()
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

    sendViewerAction()
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

    sendViewerAction()
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
  function deleteNavigators() {
    viewerNavigationMap.value.clear()
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
    const filePair = useAnnotation().getCurrentFilePair(destination.path)
    if (!filePair) {
      logger.warn(`cannot find filePair: path=${destination.path}`)
      return
    }
    const newViewer: Viewer =
      viewer.type === 'file' &&
      (!useAnnotation().isDividingChange.value ||
        destination.category === 'before')
        ? {
            id: viewerId,
            type: 'file',
            filePair,
            navigation: { ...destination },
          }
        : {
            id: viewerId,
            type: 'diff',
            filePair,
            navigation: { ...destination },
          }
    viewerNavigationMap.value.set(viewerId, {
      ...navigator,
      currentDestinationIndex: destinationIndex,
    })
    updateViewer(viewerId, newViewer)
  }

  function updateFilePairs(newFilePairOfViewers: FilePair[]) {
    const mainViewerIndex = viewers.value.findIndex(
      (viewer) => viewer.id === mainViewerId.value,
    )
    if (mainViewerIndex === -1) {
      throw new Error(`cannot find main viewer: id=${mainViewerId.value}`)
    }
    newFilePairOfViewers.forEach((filePair, index) => {
      const viewer = viewers.value[index]
      updateViewer(viewer.id, { ...viewer, filePair })
    })
    mainViewerId.value = viewers.value[mainViewerIndex].id
  }

  return {
    viewers: computed(() => viewers.value),
    mainViewerId,
    initialize,
    setup,
    createViewer,
    updateViewer,
    recreateViewer,
    duplicateViewer,
    deleteViewer,
    setNavigator,
    getNavigator,
    deleteNavigator,
    deleteNavigators,
    navigate,

    updateFilePairs,
  }
}
