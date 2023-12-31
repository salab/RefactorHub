import * as monaco from 'monaco-editor'
import { DiffCategory } from 'refactorhub'
import { FilePair } from './useAnnotation'
import { Token } from '@/apis'

export type CommonTokenSequenceType =
  | 'oneToOne'
  | 'oneToManyOrManyToOne'
  | 'manyToMany'

export type CommonTokenSequenceSetting = {
  [type in CommonTokenSequenceType]: boolean
}

class TokenDetail implements Token {
  public readonly raw: string
  public readonly path: string
  public readonly startPosition: monaco.Position
  public readonly endPosition: monaco.Position
  public readonly start: number
  public readonly end: number
  public readonly isSymbol: boolean
  public readonly isComment: boolean
  public constructor(
    token: Token,
    path: string,
    startPosition: monaco.Position,
    endPosition: monaco.Position,
  ) {
    const { raw, start, end, isSymbol, isComment } = token
    this.raw = raw
    this.start = start
    this.end = end
    this.isSymbol = isSymbol
    this.isComment = isComment
    this.path = path
    this.startPosition = startPosition
    this.endPosition = endPosition
  }

  public equals(other: TokenDetail): boolean {
    return (
      this.raw === other.raw &&
      this.path === other.path &&
      this.start === other.start &&
      this.end === other.end &&
      this.isSymbol === other.isSymbol &&
      this.isComment === other.isComment
    )
  }

  public matches(other: TokenDetail): boolean {
    return (
      this.raw === other.raw &&
      this.isSymbol === other.isSymbol &&
      this.isComment === other.isComment
    )
  }
}
class TokenSequenceWithoutCategory {
  public readonly tokenList: TokenDetail[]

  public constructor(tokenList: TokenDetail[]) {
    if (tokenList.every((token) => token.path !== tokenList[0].path)) {
      throw new Error('token sequence should be in a same file')
    }
    this.tokenList = tokenList
  }

  public get path(): string {
    return this.tokenList[0].path
  }

  public get joinedRaw(): string {
    return this.tokenList
      .map((token) => token.raw.replace('\r', '').replace('\n', ''))
      .join(' ')
  }

  public get range(): monaco.Range {
    const firstToken = this.tokenList[0]
    const lastToken = this.tokenList[this.tokenList.length - 1]
    return new monaco.Range(
      firstToken.startPosition.lineNumber,
      firstToken.startPosition.column,
      lastToken.endPosition.lineNumber,
      lastToken.endPosition.column + 1,
    )
  }

  public equals(other: TokenSequenceWithoutCategory): boolean {
    if (this.tokenList.length !== other.tokenList.length) return false
    for (let i = 0; i < this.tokenList.length; i++) {
      if (!this.tokenList[i].equals(other.tokenList[i])) return false
    }
    return true
  }

  public matches(other: TokenSequenceWithoutCategory): boolean {
    if (this.tokenList.length !== other.tokenList.length) return false
    for (let i = 0; i < this.tokenList.length; i++) {
      if (!this.tokenList[i].matches(other.tokenList[i])) return false
    }
    return true
  }

  public matchesJoinedRaw(joinedRaw: string): boolean {
    return this.joinedRaw === joinedRaw
  }
}
export class TokenSequence extends TokenSequenceWithoutCategory {
  public readonly category: DiffCategory

  public constructor(tokenList: TokenDetail[], category: DiffCategory) {
    super(tokenList)
    this.category = category
  }

  public override equals(other: TokenSequence): boolean {
    if (this.category !== other.category) return false
    return super.equals(other)
  }

  public isIn(path: string, category: DiffCategory): boolean {
    return this.path === path && this.category === category
  }
}
class TokenSequenceSet {
  private readonly sequenceSet: TokenSequence[] = []

  private _isHovered = false

  public get type(): CommonTokenSequenceType {
    const { before, after } = this.count
    if (before === 1 && after === 1) return 'oneToOne'
    if (before === 1 || after === 1) return 'oneToManyOrManyToOne'
    return 'manyToMany'
  }

  public get count(): { [category in DiffCategory]: number } {
    return {
      before: this.filterCategory('before').length,
      after: this.filterCategory('after').length,
    }
  }

  public get isHovered() {
    return this._isHovered
  }

  public toSet(): Set<TokenSequence> {
    return new Set(this.sequenceSet)
  }

  public add(tokenSequence: TokenSequence) {
    if (this.sequenceSet.every((sequence) => !sequence.equals(tokenSequence))) {
      this.sequenceSet.push(tokenSequence)
    }
  }

  public matchesSetting(setting: CommonTokenSequenceSetting) {
    return setting[this.type]
  }

  public getSequencesIn(path: string, category: DiffCategory): TokenSequence[] {
    return this.sequenceSet.filter((sequence) => sequence.isIn(path, category))
  }

  public filterCategory(category: DiffCategory) {
    return this.sequenceSet.filter((sequence) => sequence.category === category)
  }

  public updateIsHovered(
    path: string,
    category: DiffCategory,
    position: monaco.Position,
  ) {
    this._isHovered = this.sequenceSet.some(
      (sequence) =>
        sequence.isIn(path, category) &&
        sequence.range.containsPosition(position),
    )
  }
}
class CommonTokenSequenceStorage {
  private readonly commonSequenceSetMap: {
    /* key */ commonSequence: TokenSequenceWithoutCategory
    /* value */ sequenceSet: TokenSequenceSet
  }[] = []

  public add(tokenSequence: TokenSequence) {
    const commonTokensSetEntry = this.getEntryFromMap(tokenSequence)
    if (!commonTokensSetEntry) {
      const sequenceSet = new TokenSequenceSet()
      sequenceSet.add(tokenSequence)
      this.commonSequenceSetMap.push({
        commonSequence: tokenSequence,
        sequenceSet,
      })
      return
    }
    commonTokensSetEntry.sequenceSet.add(tokenSequence)
  }

  public get(setting: CommonTokenSequenceSetting): {
    joinedRaw: string
    tokenSequenceSet: TokenSequenceSet
  }[] {
    return this.commonSequenceSetMap
      .filter(({ sequenceSet }) => sequenceSet.matchesSetting(setting))
      .map((entry) => ({
        joinedRaw: entry.commonSequence.joinedRaw,
        tokenSequenceSet: entry.sequenceSet,
      }))
  }

  public getCommonTokenSequencesWithRaws(
    tokenSequenceRaw: string,
  ): Set<TokenSequence> {
    const commonTokensSetEntry = this.commonSequenceSetMap.find(
      ({ commonSequence }) => commonSequence.matchesJoinedRaw(tokenSequenceRaw),
    )
    if (!commonTokensSetEntry) return new Set()
    return commonTokensSetEntry.sequenceSet.toSet()
  }

  public updateIsHovered(
    path: string,
    category: DiffCategory,
    position: monaco.Position,
  ) {
    this.commonSequenceSetMap.forEach(({ sequenceSet }) =>
      sequenceSet.updateIsHovered(path, category, position),
    )
  }

  private getEntryFromMap(key: TokenSequenceWithoutCategory) {
    return this.commonSequenceSetMap.find(({ commonSequence }) =>
      commonSequence.matches(key),
    )
  }
}

export const useCommonTokenSequence = () => {
  const storage = useState<CommonTokenSequenceStorage>(
    'commonTokenSequenceStorage',
    () => new CommonTokenSequenceStorage(),
  )
  const setting = useState<CommonTokenSequenceSetting>(
    'commonTokenSequenceSetting',
    () => ({
      oneToOne: true,
      oneToManyOrManyToOne: false,
      manyToMany: false,
    }),
  )
  const selectedId = useState<number | undefined>('selectedId', () => undefined)

  function initialize() {
    storage.value = new CommonTokenSequenceStorage()
    setting.value = {
      oneToOne: true,
      oneToManyOrManyToOne: false,
      manyToMany: false,
    }
    selectedId.value = undefined
  }

  function setup() {
    useLoader().setLoadingText('detecting common token sequences')
    setupStorage()
  }

  function setupStorage() {
    storage.value = new CommonTokenSequenceStorage()
    const currentFilePairs = useAnnotation().getCurrentFilePairs()
    initializeStorage(storage.value, currentFilePairs)
  }

  function getCommonTokenSequencesIn(path: string, category: DiffCategory) {
    const all = storage.value.get(setting.value)
    const results: {
      sequence: TokenSequence
      id: number
      type: CommonTokenSequenceType
      count: { [category in DiffCategory]: number }
    }[] = []
    for (let id = 0; id < all.length; id++) {
      const { tokenSequenceSet } = all[id]
      const type = tokenSequenceSet.type
      const count = tokenSequenceSet.count
      const sequences = tokenSequenceSet.getSequencesIn(path, category)
      results.push(
        ...sequences.map((sequence) => ({ sequence, id, type, count })),
      )
    }
    return results
  }
  function getWithId(id: number) {
    const all = storage.value.get(setting.value)
    if (!all[id]) {
      throw new Error(
        `cannot find common token sequence; id=${id}, all.length=${all.length}`,
      )
    }
    return all[id]
  }
  function updateIsHovered(
    path: string,
    category: DiffCategory,
    position: monaco.Position,
  ) {
    storage.value.updateIsHovered(path, category, position)
  }
  function updateSetting(newSetting: CommonTokenSequenceSetting) {
    setting.value = newSetting
  }
  function updateSelectedId(newSelectedId: number | undefined) {
    selectedId.value = newSelectedId
  }

  return {
    maxId: computed(() => storage.value.get(setting.value).length - 1),
    selectedId: computed(() => selectedId.value),
    setting: computed(() => setting.value),
    initialize,
    setup,
    setupStorage,
    getCommonTokenSequencesIn,
    getWithId,
    updateIsHovered,
    updateSetting,
    updateSelectedId,
  }
}

function initializeStorage(
  storage: CommonTokenSequenceStorage,
  currentFilePairs: FilePair[],
) {
  const diffHunkTokens: {
    [category in DiffCategory]: {
      token: TokenDetail | 'diffHunkSeparator'
      isCommonToken: boolean
    }[]
  } = {
    before: [{ token: 'diffHunkSeparator', isCommonToken: false }],
    after: [{ token: 'diffHunkSeparator', isCommonToken: false }],
  }
  for (const currentFilePair of currentFilePairs) {
    const diffHunkRanges: { [category in DiffCategory]: monaco.Range[] } = {
      before: [],
      after: [],
    }
    currentFilePair.diffHunks.forEach(({ before, after }) => {
      if (before)
        diffHunkRanges.before.push(
          new monaco.Range(before.startLine, 1, before.endLine + 1, 0),
        )
      if (after)
        diffHunkRanges.after.push(
          new monaco.Range(after.startLine, 1, after.endLine + 1, 0),
        )
    })
    const beforeFile = currentFilePair.before ?? {
      text: '',
      tokens: [],
    }
    const afterFile = currentFilePair.after ?? {
      text: '',
      tokens: [],
    }
    const filePair = {
      before: beforeFile,
      after: afterFile,
    }
    function addDiffHunkTokens(category: DiffCategory) {
      const path =
        category === 'before'
          ? currentFilePair.before?.path
          : currentFilePair.after?.path
      if (path === undefined) return
      diffHunkRanges[category].forEach((diffHunkRange) => {
        const diffHunkStart = getIndexFromPosition(
          filePair[category].text,
          diffHunkRange.getStartPosition(),
        )
        const diffHunkEnd = getIndexFromPosition(
          filePair[category].text,
          diffHunkRange.getEndPosition(),
        )
        const tokenIndexStart = binarySearch(
          filePair[category].tokens,
          (token) => diffHunkStart - token.end,
          -1,
        )
        const tokenIndexEnd = binarySearch(
          filePair[category].tokens,
          (token) => diffHunkEnd - token.start,
          1,
        )
        if (
          tokenIndexStart === undefined ||
          tokenIndexEnd === undefined ||
          tokenIndexStart > tokenIndexEnd
        ) {
          return
        }
        filePair[category].tokens
          .slice(tokenIndexStart, tokenIndexEnd + 1)
          .forEach((token) => {
            const startPosition = getPositionFromIndex(
              filePair[category].text,
              token.start,
            )
            const endPosition = getPositionFromIndex(
              filePair[category].text,
              token.end,
            )
            diffHunkTokens[category].push({
              token: new TokenDetail(token, path, startPosition, endPosition),
              isCommonToken: false,
            })
          })
        diffHunkTokens[category].push({
          token: 'diffHunkSeparator',
          isCommonToken: false,
        })
      })
    }
    addDiffHunkTokens('before')
    addDiffHunkTokens('after')
  }

  const scoreTable: number[][] = [] // number[beforeIndex][afterIndex]
  const detectedCommonTokenSequences: TokenSequenceWithoutCategory[] = []
  function addDetectedCommonTokenSequence(
    sequence: TokenSequenceWithoutCategory,
  ) {
    if (detectedCommonTokenSequences.some((s) => s.matches(sequence))) return
    detectedCommonTokenSequences.push(sequence)
  }

  function registerCommonTokenSequence(beforeI: number, afterI: number) {
    if (scoreTable[beforeI][afterI] < 2) return
    const commonTokenList: TokenDetail[] = []
    let i = 0
    while (beforeI >= i && afterI >= i) {
      const token = diffHunkTokens.before[beforeI - i].token
      const score = scoreTable[beforeI - i][afterI - i]
      if (token === 'diffHunkSeparator' || score === 0) break
      diffHunkTokens.before[beforeI - i].isCommonToken = true
      diffHunkTokens.after[afterI - i].isCommonToken = true
      commonTokenList.unshift(token)
      i++
    }
    addDetectedCommonTokenSequence(
      new TokenSequenceWithoutCategory(commonTokenList),
    )
  }
  for (let beforeI = 0; beforeI < diffHunkTokens.before.length; beforeI++) {
    scoreTable[beforeI] = []
    const tokenBefore = diffHunkTokens.before[beforeI].token
    for (let afterI = 0; afterI < diffHunkTokens.after.length; afterI++) {
      const tokenAfter = diffHunkTokens.after[afterI].token
      if (beforeI === 0 || afterI === 0) {
        if (
          tokenBefore !== 'diffHunkSeparator' &&
          tokenAfter !== 'diffHunkSeparator'
        ) {
          throw new Error(
            `First row or column should be diffHunkSeparator; tokenBefore=${tokenBefore}, tokenAfter=${tokenAfter}`,
          )
        }
        scoreTable[beforeI][afterI] = 0
        continue
      }
      const preScore = scoreTable[beforeI - 1][afterI - 1]
      if (
        tokenBefore === 'diffHunkSeparator' ||
        tokenAfter === 'diffHunkSeparator' ||
        !tokenBefore.matches(tokenAfter)
      ) {
        scoreTable[beforeI][afterI] = 0
        if (preScore >= 2) registerCommonTokenSequence(beforeI - 1, afterI - 1)
        continue
      }
      if (tokenBefore.isSymbol) {
        scoreTable[beforeI][afterI] =
          preScore % 2 === 0 ? preScore + 1 : preScore
      } else {
        scoreTable[beforeI][afterI] = preScore < 2 ? preScore + 2 : preScore
      }
    }
  }

  const commonTokens: {
    [category in DiffCategory]: (TokenDetail | 'sequenceSeparator')[]
  } = {
    before: [],
    after: [],
  }
  function addCommonTokens(category: DiffCategory) {
    let alreadyAddedSequenceSeparator = false
    for (const { token, isCommonToken } of diffHunkTokens[category]) {
      if (token === 'diffHunkSeparator' || !isCommonToken) {
        if (alreadyAddedSequenceSeparator) continue
        commonTokens[category].push('sequenceSeparator')
        alreadyAddedSequenceSeparator = true
        continue
      }
      commonTokens[category].push(token)
      alreadyAddedSequenceSeparator = false
    }
  }
  addCommonTokens('before')
  addCommonTokens('after')

  const detectedCommonTokens: (TokenDetail | 'sequenceSeparator')[] = [
    'sequenceSeparator',
  ]
  for (const sequence of detectedCommonTokenSequences) {
    detectedCommonTokens.push(...sequence.tokenList, 'sequenceSeparator')
  }

  function checkAndRegisterCommonTokens(category: DiffCategory) {
    function checkCommonTokens(detectedI: number, tokenI: number) {
      if (!flagTable[detectedI][tokenI]) return
      const commonTokenList: TokenDetail[] = []
      let i = 0
      while (detectedI >= i && tokenI >= i) {
        const token = commonTokens[category][tokenI - i]
        const flag = flagTable[detectedI - i][tokenI - i]
        if (token === 'sequenceSeparator' || !flag) break
        commonTokenList.unshift(token)
        i++
      }
      const detectedToken = detectedCommonTokens[detectedI - i]
      if (detectedToken !== 'sequenceSeparator') return
      storage.add(new TokenSequence(commonTokenList, category))
    }

    const flagTable: boolean[][] = []
    for (
      let detectedI = 0;
      detectedI < detectedCommonTokens.length;
      detectedI++
    ) {
      flagTable[detectedI] = []
      const detectedToken = detectedCommonTokens[detectedI]
      for (let tokenI = 0; tokenI < commonTokens[category].length; tokenI++) {
        const commonToken = commonTokens[category][tokenI]
        if (detectedI === 0 || tokenI === 0) {
          if (
            detectedToken !== 'sequenceSeparator' &&
            commonToken !== 'sequenceSeparator'
          ) {
            throw new Error(
              `First row or column should be sequenceSeparator; detectedToken=${detectedToken}, commonToken=${commonToken}`,
            )
          }
          flagTable[detectedI][tokenI] = false
          continue
        }
        const preFlag = flagTable[detectedI - 1][tokenI - 1]
        if (detectedToken === 'sequenceSeparator') {
          flagTable[detectedI][tokenI] = false
          if (preFlag) checkCommonTokens(detectedI - 1, tokenI - 1)
          continue
        }
        flagTable[detectedI][tokenI] =
          commonToken !== 'sequenceSeparator' &&
          detectedToken.matches(commonToken)
      }
    }
  }
  checkAndRegisterCommonTokens('before')
  checkAndRegisterCommonTokens('after')
}

/**
 * @param text the entire text of the file
 * @param index 0-indexed
 */
function getPositionFromIndex(text: string, index: number): monaco.Position {
  const raws = text.split('\n')
  for (let i = 0; i < raws.length - 1; i++) raws[i] += '\n'
  let indexCount = 0
  for (let i = 0; i < raws.length; i++) {
    const raw = raws[i]
    if (indexCount <= index && index < indexCount + raw.length) {
      const line = i + 1
      const column = index - indexCount + 1
      return new monaco.Position(line, column)
    }
    indexCount += raw.length
  }
  throw new Error(`index ${index} must be less than text length ${text.length}`)
}
function getIndexFromPosition(text: string, position: monaco.Position): number {
  const raws = text.split('\n')
  for (let i = 0; i < raws.length - 1; i++) raws[i] += '\n'
  let indexCount = 0
  for (let i = 0; i < raws.length; i++) {
    const raw = raws[i]
    if (i === position.lineNumber - 1) {
      indexCount += position.column - 1
      return indexCount
    }
    indexCount += raw.length
  }
  if (position.lineNumber === raws.length + 1 && position.column === 0) {
    return indexCount
  }
  throw new Error(
    `position ${position.toString()} must be less than text which has ${
      raws.length
    } lines`,
  )
}

function binarySearch<T>(
  sortedArray: T[],
  compare: (element: T) => number,
  needs: -1 | 1,
): number | undefined {
  if (sortedArray.length === 0) return undefined

  let left = 0
  let right = sortedArray.length - 1

  while (left <= right) {
    const mid = Math.floor((left + right) / 2)
    const difference = compare(sortedArray[mid])
    if (difference < 0) {
      right = mid - 1
    } else if (difference > 0) {
      left = mid + 1
    } else {
      return mid
    }
  }

  if (right === -1) {
    const firstDifference = compare(sortedArray[0])
    if (firstDifference < 0 && needs < 0) return 0
    if (firstDifference > 0 && needs > 0) return 0
    return undefined
  }

  if (left === sortedArray.length) {
    const lastDifference = compare(sortedArray[sortedArray.length - 1])
    if (lastDifference < 0 && needs < 0) return sortedArray.length - 1
    if (lastDifference > 0 && needs > 0) return sortedArray.length - 1
    return undefined
  }

  const leftDifference = compare(sortedArray[left])
  const rightDifference = compare(sortedArray[right])
  if (needs < 0) {
    if (rightDifference < 0) return right
    if (leftDifference < 0) return left
    return undefined
  }

  if (leftDifference > 0) return left
  if (rightDifference > 0) return right
  return undefined
}
