import * as monaco from 'monaco-editor'
import { DiffCategory } from 'refactorhub'
import apis, {
  Annotation,
  Change,
  ChangeType,
  DiffHunk,
  FileMapping,
  FileMappingStatusEnum,
  FileModel,
  ParameterData,
  Range,
  Snapshot,
} from '@/apis'
import { useState } from '#imports'
import { asMonacoRange } from '@/components/common/editor/utils/range'

export interface ChangeParameters {
  changeId: string
  parameters:
    | 'all'
    | {
        category: DiffCategory
        parameterName: string
        elementIndex: number
      }[]
}
export interface ChangeParametersTextModels {
  changeId: string
  changeType: ChangeType
  diff: {
    parameterName: string
    before: {
      description: string
      model: monaco.editor.ITextModel
    }
    after: {
      description: string
      model: monaco.editor.ITextModel
    }
  }[]
  before: {
    parameterName: string
    description: string
    model: monaco.editor.ITextModel
  }[]
  after: {
    parameterName: string
    description: string
    model: monaco.editor.ITextModel
  }[]
}

interface PartialPathPair {
  readonly before?: string
  readonly after?: string
  readonly notFound?: string
}
interface PathPairBothExist extends PartialPathPair {
  readonly before: string
  readonly after: string
  readonly notFound?: undefined
}
interface PathPairOfRemoved extends PartialPathPair {
  readonly before: string
  readonly after?: undefined
  readonly notFound?: undefined
}
interface PathPairOfAdded extends PartialPathPair {
  readonly before?: undefined
  readonly after: string
  readonly notFound?: undefined
}
interface PathPairOfNotFound extends PartialPathPair {
  readonly before?: undefined
  readonly after?: undefined
  readonly notFound: string
}
export type PathPair =
  | PathPairBothExist
  | PathPairOfRemoved
  | PathPairOfAdded
  | PathPairOfNotFound

interface AbstractFilePair<T> {
  prev?: T
  next?: T
  readonly snapshotId: string
  readonly status: `${FileMappingStatusEnum}` | 'not found'
  readonly before?: FileModel
  readonly after?: FileModel
  readonly notFoundPath?: string
  readonly diffHunks: DiffHunk[]
  getFilePairAt(snapshotId: string): T
  getPathPair(): PathPair
  getEdgePathPair(): PathPair
  isNotRemovedYet(): boolean
  isAlreadyRemoved(): boolean
}
interface FilePairModified<T> extends AbstractFilePair<T> {
  readonly status: 'modified'
  readonly before: FileModel
  readonly after: FileModel
  readonly notFoundPath?: undefined
  getPathPair(): PathPairBothExist
}
interface FilePairAdded<T> extends AbstractFilePair<T> {
  readonly status: 'added'
  readonly before?: undefined
  readonly after: FileModel
  readonly notFoundPath?: undefined
  getPathPair(): PathPairOfAdded
}
interface FilePairRemoved<T> extends AbstractFilePair<T> {
  readonly status: 'removed'
  readonly before: FileModel
  readonly after?: undefined
  readonly notFoundPath?: undefined
  getPathPair(): PathPairOfRemoved
}
interface FilePairRenamed<T> extends AbstractFilePair<T> {
  readonly status: 'renamed'
  readonly before: FileModel
  readonly after: FileModel
  readonly notFoundPath?: undefined
  getPathPair(): PathPairBothExist
}
interface FilePairUnmodified<T> extends AbstractFilePair<T> {
  readonly status: 'unmodified'
  readonly before: FileModel
  readonly after: FileModel
  readonly notFoundPath?: undefined
  getPathPair(): PathPairBothExist
}
interface FilePairNotFound<T> extends AbstractFilePair<T> {
  readonly status: 'not found'
  readonly before?: undefined
  readonly after?: undefined
  readonly notFoundPath: string
  getPathPair(): PathPairOfNotFound
}
export type FilePair =
  | FilePairModified<FilePair>
  | FilePairAdded<FilePair>
  | FilePairRemoved<FilePair>
  | FilePairRenamed<FilePair>
  | FilePairUnmodified<FilePair>
  | FilePairNotFound<FilePair>

abstract class AbstractFilePairImpl implements AbstractFilePair<FilePair> {
  public prev: FilePair | undefined
  public next: FilePair | undefined
  public readonly snapshotId: string
  public abstract readonly status:
    | 'modified'
    | 'added'
    | 'removed'
    | 'renamed'
    | 'unmodified'
    | 'not found'

  public abstract readonly before: FileModel | undefined
  public abstract readonly after: FileModel | undefined
  public abstract readonly notFoundPath: string | undefined
  public readonly diffHunks: DiffHunk[]
  constructor(snapshotId: string, diffHunks: DiffHunk[]) {
    this.snapshotId = snapshotId
    this.diffHunks = diffHunks
  }

  public getFilePairAt(snapshotId: string): FilePair {
    if (this.snapshotId === snapshotId) return this as FilePair
    let node = this.prev
    while (node) {
      if (node.snapshotId === snapshotId) return node
      node = node.prev
    }

    node = this.next
    while (node) {
      if (node.snapshotId === snapshotId) return node
      node = node.next
    }

    throw new Error(`there is no filePair at snapshotId=${snapshotId}`)
  }

  public abstract getPathPair(): PathPair
  public getEdgePathPair(): PathPair {
    let prevEdgePath = this.before?.path
    let node = this.prev
    while (node) {
      if (node.before?.path !== undefined) prevEdgePath = node.before?.path
      node = node.prev
    }

    let nextEdgePath = this.after?.path
    node = this.next
    while (node) {
      if (node.after?.path !== undefined) nextEdgePath = node.after?.path
      node = node.next
    }

    if (prevEdgePath !== undefined)
      return {
        before: prevEdgePath,
        after: nextEdgePath,
        notFound: undefined,
      }
    if (nextEdgePath !== undefined)
      return {
        before: prevEdgePath,
        after: nextEdgePath,
        notFound: undefined,
      }

    logger.error(this)
    throw new Error(`there is no edge path`)
  }

  public isNotRemovedYet(): boolean {
    if (this.status === 'removed' || this.status === 'renamed') return true
    let node = this.next
    while (node) {
      if (node.status === 'removed' || node.status === 'renamed') return true
      node = node.next
    }
    return false
  }

  public isAlreadyRemoved(): boolean {
    let node = this.prev
    while (node) {
      if (node.status === 'removed') return true
      node = node.prev
    }
    return false
  }
}
class FilePairModifiedImpl
  extends AbstractFilePairImpl
  implements FilePairModified<FilePair>
{
  public readonly status = 'modified'
  public readonly before: FileModel
  public readonly after: FileModel
  public readonly notFoundPath = undefined
  constructor(
    snapshotId: string,
    diffHunks: DiffHunk[],
    before: FileModel,
    after: FileModel,
  ) {
    super(snapshotId, diffHunks)
    if (before.path !== after.path)
      throw new Error(
        `before and after paths of modified file must be same; before=${before.path}, after=${after.path}`,
      )
    this.before = before
    this.after = after
  }

  public getPathPair(): PathPairBothExist {
    return {
      before: this.before.path,
      after: this.after.path,
    }
  }
}
class FilePairAddedImpl
  extends AbstractFilePairImpl
  implements FilePairAdded<FilePair>
{
  public readonly status = 'added'
  public readonly before = undefined
  public readonly after: FileModel
  public readonly notFoundPath = undefined
  constructor(snapshotId: string, diffHunks: DiffHunk[], after: FileModel) {
    super(snapshotId, diffHunks)
    this.after = after
  }

  public getPathPair(): PathPairOfAdded {
    return {
      after: this.after.path,
    }
  }
}
class FilePairRemovedImpl
  extends AbstractFilePairImpl
  implements FilePairRemoved<FilePair>
{
  public readonly status = 'removed'
  public readonly before: FileModel
  public readonly after = undefined
  public readonly notFoundPath = undefined
  constructor(snapshotId: string, diffHunks: DiffHunk[], before: FileModel) {
    super(snapshotId, diffHunks)
    this.before = before
  }

  public getPathPair(): PathPairOfRemoved {
    return {
      before: this.before.path,
    }
  }
}
class FilePairRenamedImpl
  extends AbstractFilePairImpl
  implements FilePairRenamed<FilePair>
{
  public readonly status = 'renamed'
  public readonly before: FileModel
  public readonly after: FileModel
  public readonly notFoundPath = undefined
  constructor(
    snapshotId: string,
    diffHunks: DiffHunk[],
    before: FileModel,
    after: FileModel,
  ) {
    super(snapshotId, diffHunks)
    if (before.path === after.path)
      throw new Error(
        `before and after paths of renamed file must be different; before=${before.path}, after=${after.path}`,
      )
    this.before = before
    this.after = after
  }

  public getPathPair(): PathPairBothExist {
    return {
      before: this.before.path,
      after: this.after.path,
    }
  }
}
class FilePairUnmodifiedImpl
  extends AbstractFilePairImpl
  implements FilePairUnmodified<FilePair>
{
  public readonly status = 'unmodified'
  public readonly before: FileModel
  public readonly after: FileModel
  public readonly notFoundPath = undefined
  constructor(
    snapshotId: string,
    diffHunks: DiffHunk[],
    before: FileModel,
    after: FileModel,
  ) {
    super(snapshotId, diffHunks)
    if (before.text !== after.text)
      throw new Error(
        `before and after texts of unmodified file must be same; before=${before.text}, after=${after.text}`,
      )
    this.before = before
    this.after = after
  }

  public getPathPair(): PathPairBothExist {
    return {
      before: this.before.path,
      after: this.after.path,
    }
  }
}
class FilePairNotFoundImpl
  extends AbstractFilePairImpl
  implements FilePairNotFound<FilePair>
{
  public readonly status = 'not found'
  public readonly before = undefined
  public readonly after = undefined
  public readonly notFoundPath: string
  constructor(snapshotId: string, diffHunks: DiffHunk[], notFoundPath: string) {
    super(snapshotId, diffHunks)
    this.notFoundPath = notFoundPath
  }

  public getPathPair(): PathPairOfNotFound {
    return { notFound: this.notFoundPath }
  }
}

function createFilePair(
  fileMapping: FileMapping,
  before: FileModel | undefined,
  after: FileModel | undefined,
  snapshotId: string,
): FilePair {
  function getFile(category: DiffCategory): FileModel {
    const file = category === 'before' ? before : after
    if (file === undefined)
      throw new Error(
        `file ${
          category === 'before' ? fileMapping.beforePath : fileMapping.afterPath
        } is not found at ${snapshotId}`,
      )
    return file
  }
  switch (fileMapping.status) {
    case FileMappingStatusEnum.Modified:
      return new FilePairModifiedImpl(
        snapshotId,
        fileMapping.diffHunks,
        getFile('before'),
        getFile('after'),
      )
    case FileMappingStatusEnum.Added:
      return new FilePairAddedImpl(
        snapshotId,
        fileMapping.diffHunks,
        getFile('after'),
      )
    case FileMappingStatusEnum.Removed:
      return new FilePairRemovedImpl(
        snapshotId,
        fileMapping.diffHunks,
        getFile('before'),
      )
    case FileMappingStatusEnum.Renamed:
      return new FilePairRenamedImpl(
        snapshotId,
        fileMapping.diffHunks,
        getFile('before'),
        getFile('after'),
      )
    case FileMappingStatusEnum.Unmodified:
      return new FilePairUnmodifiedImpl(
        snapshotId,
        fileMapping.diffHunks,
        getFile('before'),
        getFile('after'),
      )
  }
}
function calcFilePairsMap(annotation: Annotation): Map<string, FilePair[]> {
  /**
   * list[0] has a map of commit.beforeFiles, and
   * list[i > 0] has a map of snapshots[i - 1].files
   */
  const fileMapList = [
    annotation.commit.beforeFiles,
    ...annotation.snapshots.map((snapshot) => snapshot.files),
  ].map(
    (files) =>
      new Map<string, FileModel>(files.map((file) => [file.path, file])),
  )

  const filePairsEntries: [string, FilePair[]][] = []
  for (let i = 0; i < annotation.snapshots.length; i++) {
    const snapshot = annotation.snapshots[i]
    filePairsEntries[i] = [snapshot.id, []]
    for (const fileMapping of snapshot.fileMappings) {
      const before = fileMapping.beforePath
        ? fileMapList[i].get(fileMapping.beforePath)
        : undefined
      const after = fileMapping.afterPath
        ? fileMapList[i + 1].get(fileMapping.afterPath)
        : undefined
      const filePair = createFilePair(fileMapping, before, after, snapshot.id)
      filePairsEntries[i][1].push(filePair)
      if (i > 0) {
        const current = filePair.getPathPair()
        const prevFilePair = filePairsEntries[i - 1][1].find((prevFilePair) => {
          const prev = prevFilePair.getPathPair()
          return (
            (prev.after !== undefined && prev.after === current.before) ||
            (prev.notFound !== undefined && prev.notFound === current.before)
          )
        })
        if (prevFilePair) {
          filePair.prev = prevFilePair
          prevFilePair.next = filePair
        } else {
          const notFoundPath =
            current.after ?? current.before ?? current.notFound
          if (notFoundPath === undefined)
            throw new Error('filePair should have path information')
          let j = i - 1
          let node = filePair
          while (j >= 0) {
            const snapshotId = annotation.snapshots[j].id
            const notFound = new FilePairNotFoundImpl(
              snapshotId,
              [],
              notFoundPath,
            )
            filePairsEntries[j][1].push(notFound)
            node.prev = notFound
            notFound.next = node
            node = notFound
            j--
          }
        }
      }
    }
    if (i === 0) continue
    filePairsEntries[i - 1][1]
      .filter((prevFilePair) => prevFilePair.next === undefined)
      .forEach((prevFilePair) => {
        const pathPair = prevFilePair.getPathPair()
        const notFoundPath =
          pathPair.notFound ?? pathPair.before ?? pathPair.after
        if (notFoundPath === undefined)
          throw new Error('filePair should have path information')
        const filePair = new FilePairNotFoundImpl(snapshot.id, [], notFoundPath)
        filePairsEntries[i][1].push(filePair)
        filePair.prev = prevFilePair
        prevFilePair.next = filePair
      })
  }

  filePairsEntries.forEach(([snapshotId, filePairs]) => {
    if (filePairs.length !== filePairsEntries[0][1].length)
      throw new Error(
        `number of file pairs should be same; first:${filePairsEntries[0][1].length}, at snapshotId=${snapshotId}:${filePairs.length}`,
      )
  })
  return new Map(filePairsEntries)
}

export const useAnnotation = () => {
  const annotation = useState<Annotation | undefined>(
    'annotation',
    () => undefined,
  )
  /** Map<snapshotId, (FilePair whose snapshotId = key)[]> */
  const filePairsMap = useState<Map<string, FilePair[]>>(
    'filePairsMap',
    () => new Map(),
  )
  const changeTypes = useState<ChangeType[]>('changeTypes', () => [])
  const codeElementTypes = useState<string[]>('codeElementTypes', () => [])

  const currentChangeId = useState<string | undefined>(
    'currentChangeId',
    () => undefined,
  )
  const currentSnapshot = computed(
    () =>
      annotation.value?.snapshots.find((snapshot) =>
        snapshot.changes.some((change) => change.id === currentChangeId.value),
      ),
  )
  const currentChange = computed(
    () =>
      currentSnapshot.value?.changes.find(
        (change) => change.id === currentChangeId.value,
      ),
  )
  const currentIds = computed(() => {
    const annotationId = annotation.value?.id
    const snapshotId = currentSnapshot.value?.id
    const changeId = currentChange.value?.id
    return { annotationId, snapshotId, changeId }
  })
  const isOwner = computed(
    () => (annotation.value?.ownerId ?? '') === useUser().user.value?.id,
  )
  const currentIsLast = computed(
    () =>
      getChangeList().length > 0 &&
      getChangeList()[getChangeList().length - 1].id ===
        currentChange.value?.id,
  )
  const canEditCurrentChange = computed(() => {
    return annotation.value?.hasTemporarySnapshot && currentIsLast.value
      ? false
      : isOwner && !!annotation.value?.isDraft
  })

  function updateFilePairsMap() {
    filePairsMap.value = annotation.value
      ? calcFilePairsMap(annotation.value)
      : new Map()
  }

  function initialize() {
    annotation.value = undefined
    filePairsMap.value = new Map()
    changeTypes.value = []
    codeElementTypes.value = []
    currentChangeId.value = ''
  }

  async function setup(annotationId: string): Promise<FilePair> {
    useLoader().setLoadingText('loading annotation data')
    annotation.value = (await apis.annotations.getAnnotation(annotationId)).data
    useLoader().setLoadingText('loading change types data')
    changeTypes.value = (await apis.changeTypes.getChangeTypes()).data
    useLoader().setLoadingText('loading code element types data')
    codeElementTypes.value = (
      await apis.codeElementTypes.getCodeElementTypes()
    ).data
    useLoader().setLoadingText('setting change to show')
    currentChangeId.value = getChangeList()[0].id
    useLoader().setLoadingText('building file transition data')
    updateFilePairsMap()
    const initialFilePair = filePairsMap.value.get(
      annotation.value.snapshots[0].id,
    )?.[0]
    if (!initialFilePair)
      throw new Error(
        `filePairsMap may be empty: filePairsMap.size=${filePairsMap.value.size}`,
      )
    return initialFilePair
  }

  function getChangeList() {
    if (!annotation.value) return []
    return annotation.value.snapshots.flatMap((snapshot) => snapshot.changes)
  }

  function getEdgeFilePair(edgePathPair: PathPair) {
    if (!annotation.value) return
    const snapshots = annotation.value.snapshots
    if (edgePathPair.before !== undefined) {
      const snapshotId = snapshots[0].id
      const edgeFilePair = filePairsMap.value
        .get(snapshotId)
        ?.find((filePair) => filePair.before?.path === edgePathPair.before)
      if (edgeFilePair) return edgeFilePair
    }
    if (edgePathPair.after !== undefined) {
      const snapshotId = snapshots[snapshots.length - 1].id
      const edgeFilePair = filePairsMap.value
        .get(snapshotId)
        ?.find((filePair) => filePair.after?.path === edgePathPair.after)
      if (edgeFilePair) return edgeFilePair
    }
  }
  function getCurrentFilePairs(): FilePair[] {
    const snapshot = currentSnapshot.value
    if (!snapshot) return []
    return filePairsMap.value.get(snapshot.id) ?? []
  }
  function getCurrentFilePair(currentPath: string): FilePair | undefined {
    return getCurrentFilePairs().find((filePair) => {
      const pathPair = filePair.getPathPair()
      return (
        pathPair.before === currentPath ||
        pathPair.after === currentPath ||
        pathPair.notFound === currentPath
      )
    })
  }
  function getTextModel(filePair: FilePair, category: DiffCategory) {
    const file = filePair[category]
    const path =
      file?.path ??
      filePair.before?.path ??
      filePair.after?.path ??
      filePair.notFoundPath
    if (path === undefined) {
      throw new Error('filePair should have path information')
    }
    const text = file?.text ?? ''
    const extension = file?.extension ?? 'text/plain'
    return monaco.editor.createModel(text, extension)
  }
  function getChangeParametersTextModels(
    changeParametersList: ChangeParameters[],
  ): ChangeParametersTextModels[] {
    if (!annotation.value) return []
    function getParameters(
      parameterData: ParameterData,
      changeType: ChangeType,
      category: DiffCategory,
    ) {
      const entries = Object.entries(parameterData[category])
      const map = changeType[category]
      entries.sort((a, b) => {
        if (a[0] in map && b[0] in map) {
          if (map[a[0]].required && !map[b[0]].required) return -1
          if (!map[a[0]].required && map[b[0]].required) return 1
        } else if (a[0] in map) return -1
        else if (b[0] in map) return 1
        return a[0] < b[0] ? -1 : a[0] > b[0] ? 1 : 0
      })
      const elementHolderMap = Object.fromEntries(entries)

      const parameters: {
        parameterName: string
        description: string
        locations: { path: string; range: Range }[]
      }[] = []
      for (const [parameterName, elementHolder] of Object.entries(
        elementHolderMap,
      )) {
        const description =
          changeType[category][parameterName]?.description ?? ''
        const locations = []
        for (const element of elementHolder.elements) {
          const path = element.location?.path
          const range = element.location?.range
          if (!path || !range) continue
          locations.push({ path, range })
        }
        parameters.push({ parameterName, description, locations })
      }
      return parameters
    }
    const snapshots = annotation.value.snapshots
    const commit = annotation.value.commit
    const results: ChangeParametersTextModels[] = []
    changeParametersList.forEach(({ changeId, parameters: filters }) => {
      const snapshotIndex = snapshots.findIndex((snapshot) =>
        snapshot.changes.some(({ id }) => id === changeId),
      )
      if (snapshotIndex === -1) return
      const snapshot = snapshots[snapshotIndex]
      const change = snapshot.changes.find(({ id }) => id === changeId)
      if (!change) return
      const files = {
        before:
          snapshotIndex === 0
            ? commit.beforeFiles
            : snapshots[snapshotIndex - 1].files,
        after: snapshot.files,
      }
      const changeType = changeTypes.value.find(
        ({ name }) => name === change.typeName,
      )
      if (!changeType) return
      let parametersBefore = getParameters(
        change.parameterData,
        changeType,
        'before',
      )
      let parametersAfter = getParameters(
        change.parameterData,
        changeType,
        'after',
      )
      if (filters !== 'all') {
        parametersBefore = parametersBefore.filter((parameter) =>
          filters.some(
            (filter) =>
              filter.category === 'before' &&
              filter.parameterName === parameter.parameterName,
          ),
        )
        parametersBefore = parametersBefore.map((parameter) => {
          const indicesData = filters.filter(
            (filter) =>
              filter.category === 'before' &&
              filter.parameterName === parameter.parameterName,
          )
          return {
            ...parameter,
            locations: parameter.locations.filter((_, index) =>
              indicesData.some((indexData) => indexData.elementIndex === index),
            ),
          }
        })
        parametersAfter = parametersAfter.filter((parameter) =>
          filters.some(
            (filter) =>
              filter.category === 'after' &&
              filter.parameterName === parameter.parameterName,
          ),
        )
        parametersAfter = parametersAfter.map((parameter) => {
          const indicesData = filters.filter(
            (filter) =>
              filter.category === 'after' &&
              filter.parameterName === parameter.parameterName,
          )
          return {
            ...parameter,
            locations: parameter.locations.filter((_, index) =>
              indicesData.some((indexData) => indexData.elementIndex === index),
            ),
          }
        })
      }

      const diff: ChangeParametersTextModels['diff'] = []
      ;[...parametersBefore].forEach((before) => {
        const after = parametersAfter.find(
          (after) => after.parameterName === before.parameterName,
        )
        if (!after) return
        const texts = { before: '', after: '' }
        before.locations.forEach((location) => {
          const file = files.before.find((file) => file.path === location.path)
          if (!file) return
          const model = monaco.editor.createModel(file.text, file.extension)
          texts.before +=
            (texts.before ? '\n' : '') +
            model.getValueInRange(asMonacoRange(location.range))
        })
        after.locations.forEach((location) => {
          const file = files.after.find((file) => file.path === location.path)
          if (!file) return
          const model = monaco.editor.createModel(file.text, file.extension)
          texts.after +=
            (texts.after ? '\n' : '') +
            model.getValueInRange(asMonacoRange(location.range))
        })
        const textModels = {
          before: monaco.editor.createModel(texts.before, 'java'),
          after: monaco.editor.createModel(texts.after, 'java'),
        }
        diff.push({
          parameterName: before.parameterName,
          before: { description: before.description, model: textModels.before },
          after: { description: after.description, model: textModels.after },
        })
        parametersBefore = parametersBefore.filter(
          (parameter) => parameter.parameterName !== before.parameterName,
        )
        parametersAfter = parametersAfter.filter(
          (parameter) => parameter.parameterName !== after.parameterName,
        )
      })

      const before: ChangeParametersTextModels['before'] = parametersBefore.map(
        (parameter) => {
          let text = ''
          parameter.locations.forEach((location) => {
            const file = files.before.find(
              (file) => file.path === location.path,
            )
            if (!file) return
            const model = monaco.editor.createModel(file.text, file.extension)
            text +=
              (text ? '\n' : '') +
              model.getValueInRange(asMonacoRange(location.range))
          })
          return {
            parameterName: parameter.parameterName,
            description: parameter.description,
            model: monaco.editor.createModel(text, 'java'),
          }
        },
      )
      const after: ChangeParametersTextModels['after'] = parametersAfter.map(
        (parameter) => {
          let text = ''
          parameter.locations.forEach((location) => {
            const file = files.after.find((file) => file.path === location.path)
            if (!file) return
            const model = monaco.editor.createModel(file.text, file.extension)
            text +=
              (text ? '\n' : '') +
              model.getValueInRange(asMonacoRange(location.range))
          })
          return {
            parameterName: parameter.parameterName,
            description: parameter.description,
            model: monaco.editor.createModel(text, 'java'),
          }
        },
      )

      results.push({ changeId, changeType, diff, before, after })
    })
    return results
  }

  function updateAnnotation(
    newAnnotation: Partial<Annotation>,
    divisionIsUpdated: boolean,
  ) {
    if (!annotation.value) return
    const oldAnnotation = annotation.value
    const oldSnapshotId = currentSnapshot.value?.id
    const oldSnapshotIndex = oldAnnotation.snapshots.findIndex(
      ({ id }) => id === oldSnapshotId,
    )
    annotation.value = {
      ...annotation.value,
      ...newAnnotation,
    }
    if (!divisionIsUpdated) return

    const changeList = getChangeList()
    if (changeList.every(({ id }) => id !== currentChangeId.value)) {
      let snapshot = annotation.value.snapshots.find(
        ({ id }) => id === oldSnapshotId,
      )
      if (!snapshot) snapshot = annotation.value.snapshots[oldSnapshotIndex]
      if (snapshot) {
        currentChangeId.value = snapshot.changes[snapshot.changes.length - 1].id
      } else {
        currentChangeId.value = changeList[changeList.length - 1].id
      }
    }

    updateFilePairsMap()

    useCommonTokenSequence().setupStorage()

    const viewers = useViewer().viewers.value
    const newFilePairs = viewers.map((viewer) => {
      const edgePathPair = viewer.filePair.getEdgePathPair()
      const edgeFilePair = getEdgeFilePair(edgePathPair)
      if (!edgeFilePair)
        throw new Error(`cannot find file pair of edge of viewer ${viewer.id}`)
      if (!currentSnapshot.value)
        throw new Error('current snapshot is undefined when annotation updates')
      return edgeFilePair.getFilePairAt(currentSnapshot.value.id)
    })
    useViewer().updateFilePairs(newFilePairs)
  }
  function updateSnapshot(newSnapshot: Snapshot, divisionIsUpdated: boolean) {
    if (!annotation.value) return
    const index = annotation.value.snapshots.findIndex(
      (snapshot) => snapshot.id === newSnapshot.id,
    )
    if (index === -1) return
    const newSnapshots = [...annotation.value.snapshots]
    newSnapshots.splice(index, 1, newSnapshot)
    updateAnnotation({ snapshots: newSnapshots }, divisionIsUpdated)
  }
  function updateChange(newChange: Change) {
    if (!annotation.value) return
    const snapshot = annotation.value.snapshots.find((snapshot) =>
      snapshot.changes.some((change) => change.id === newChange.id),
    )
    if (!snapshot) return
    const index = snapshot.changes.findIndex(
      (change) => change.id === newChange.id,
    )
    if (index === -1) return
    const newChanges = [...snapshot.changes]
    newChanges.splice(index, 1, newChange)
    updateSnapshot({ ...snapshot, changes: newChanges }, false)
  }

  function updateCurrentChangeId(newChangeId: string) {
    const oldChangeId = currentChangeId.value
    if (oldChangeId === undefined) {
      currentChangeId.value = newChangeId
      return
    }
    const oldSnapshot = currentSnapshot.value
    currentChangeId.value = newChangeId
    const newSnapshot = currentSnapshot.value
    if (!newSnapshot || oldSnapshot?.id === newSnapshot.id) return

    useCommonTokenSequence().setupStorage()

    const viewers = useViewer().viewers.value
    const newFilePairs = viewers.map((viewer) =>
      viewer.filePair.getFilePairAt(newSnapshot.id),
    )
    useViewer().updateFilePairs(newFilePairs)
  }

  return {
    annotation,
    changeTypes,
    codeElementTypes,
    currentSnapshot,
    currentChange,
    currentIds,
    canEditCurrentChange,
    isDividingChange: computed(
      () =>
        !!annotation.value?.hasTemporarySnapshot &&
        getChangeList()[getChangeList().length - 2]?.id ===
          currentChangeId.value,
    ),
    initialize,
    setup,
    getChangeList,
    getCurrentFilePairs,
    getCurrentFilePair,
    getTextModel,
    getChangeParametersTextModels,
    updateAnnotation,
    updateSnapshot,
    updateChange,
    updateCurrentChangeId,
  }
}
