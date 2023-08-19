import * as monaco from 'monaco-editor'
import { DecorationMetadata, DiffCategory } from 'refactorhub'
import { Position, Range } from 'monaco-editor'
import { Token } from '@/apis'
import { TokenSequence } from 'composables/useCommonTokenSequence'

export type CommonTokensType =
  | 'oneToOne'
  | 'oneToManyOrManyToOne'
  | 'manyToMany'

class TokenDetail implements Token {
  public readonly raw: string
  public readonly path: string
  public readonly startPosition: Position
  public readonly endPosition: Position
  public readonly start: number
  public readonly end: number
  public readonly isSymbol: boolean
  public readonly isComment: boolean
  public constructor(
    token: Token,
    path: string,
    startPosition: Position,
    endPosition: Position,
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
class TokenDetailList {
  public readonly tokenList: TokenDetail[]
  public constructor(tokenList: TokenDetail[]) {
    this.tokenList = tokenList
  }

  public get joinedRaw(): string {
    return this.tokenList
      .map((token) => token.raw.replace('\r', '').replace('\n', ''))
      .join(' ')
  }

  public equals(other: TokenDetailList): boolean {
    if (this.tokenList.length !== other.tokenList.length) return false
    for (let i = 0; i < this.tokenList.length; i++) {
      if (!this.tokenList[i].equals(other.tokenList[i])) return false
    }
    return true
  }

  public matches(other: TokenDetailList): boolean {
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
export class CommonTokens {
  public readonly tokens: TokenDetailList
  public readonly path: string
  public readonly category: DiffCategory
  public constructor(tokens: TokenDetailList, category: DiffCategory) {
    this.tokens = tokens
    this.path = tokens.tokenList[0].path
    this.category = category
  }

  public get range(): Range {
    const firstToken = this.tokens.tokenList[0]
    const lastToken = this.tokens.tokenList[this.tokens.tokenList.length - 1]
    return new Range(
      firstToken.startPosition.lineNumber,
      firstToken.startPosition.column,
      lastToken.endPosition.lineNumber,
      lastToken.endPosition.column + 1,
    )
  }

  public equals(other: CommonTokens): boolean {
    if (this.path !== other.path) return false
    if (this.category !== other.category) return false
    return this.tokens.equals(other.tokens)
  }

  public isIn(path: string, category: DiffCategory): boolean {
    return this.path === path && this.category === category
  }
}
class CommonTokensSetMap {
  private readonly commonTokensSetMap: {
    /* key */ tokens: TokenDetailList
    /* value */ commonTokensSet: CommonTokens[]
  }[] = []

  /** the sha of analyzed commit */
  private _sha = ''

  public get sha() {
    return this._sha
  }

  public add(commonTokens: CommonTokens) {
    const commonTokensSetEntry = this.commonTokensSetMap.find(({ tokens }) =>
      tokens.matches(commonTokens.tokens),
    )
    if (!commonTokensSetEntry) {
      this.commonTokensSetMap.push({
        tokens: commonTokens.tokens,
        commonTokensSet: [commonTokens],
      })
      return
    }
    if (
      commonTokensSetEntry.commonTokensSet.every((c) => !c.equals(commonTokens))
    ) {
      commonTokensSetEntry.commonTokensSet.push(commonTokens)
    }
  }

  public getCommonTokensListIn(
    path: string,
    category: DiffCategory,
  ): CommonTokens[] {
    const results: CommonTokens[] = []
    for (const { commonTokensSet } of this.commonTokensSetMap) {
      for (const commonTokens of commonTokensSet) {
        if (commonTokens.isIn(path, category)) results.push(commonTokens)
      }
    }
    return results
  }

  public getCommonTokensSet(tokens: TokenDetailList): Set<CommonTokens> {
    const commonTokensSetEntry = this.commonTokensSetMap.find((entry) =>
      entry.tokens.matches(tokens),
    )
    if (!commonTokensSetEntry) return new Set()
    return new Set(commonTokensSetEntry.commonTokensSet)
  }

  public getCommonTokensSetWithRaws(
    commonTokensRaw: string,
  ): Set<CommonTokens> {
    const commonTokensSetEntry = this.commonTokensSetMap.find(({ tokens }) =>
      tokens.matchesJoinedRaw(commonTokensRaw),
    )
    if (!commonTokensSetEntry) return new Set()
    return new Set(commonTokensSetEntry.commonTokensSet)
  }

  public getCommonTokensSetId(tokens: TokenDetailList): number {
    return this.commonTokensSetMap.findIndex((entry) =>
      entry.tokens.matches(tokens),
    )
  }

  public reset(sha: string) {
    this._sha = sha
    this.commonTokensSetMap.length = 0
  }
}

const commonTokensSetMap = new CommonTokensSetMap()

export async function initCommonTokensMap() {
  const commit = useDraft().commit.value
  if (!commit) return
  const { owner, repository, files, sha } = commit
  if (sha === commonTokensSetMap.sha) return // no need for re-analyze
  commonTokensSetMap.reset(sha)

  const diffHunkTokens: {
    [category in DiffCategory]: {
      token: TokenDetail | 'diffHunkSeparator'
      isCommonToken: boolean
    }[]
  } = {
    before: [{ token: 'diffHunkSeparator', isCommonToken: false }],
    after: [{ token: 'diffHunkSeparator', isCommonToken: false }],
  }
  for (const file of files) {
    const diffHunkRanges: { [category in DiffCategory]: Range[] } = {
      before: [],
      after: [],
    }
    file.diffHunks.forEach(({ before, after }) => {
      if (before)
        diffHunkRanges.before.push(
          new Range(before.startLine, 1, before.endLine + 1, 0),
        )
      if (after)
        diffHunkRanges.after.push(
          new Range(after.startLine, 1, after.endLine + 1, 0),
        )
    })
    const fileContentPair = {
      before: await useDraft().getFileContent({
        owner,
        repository,
        sha: commit.sha,
        category: 'before',
        path: file.previousName,
        uri: getCommitFileUri(
          commit.owner,
          commit.repository,
          commit.parent,
          file.previousName,
        ),
      }),
      after: await useDraft().getFileContent({
        owner,
        repository,
        sha: commit.sha,
        category: 'after',
        path: file.name,
        uri: getCommitFileUri(
          commit.owner,
          commit.repository,
          commit.sha,
          file.name,
        ),
      }),
    }
    function addDiffHunkTokens(category: DiffCategory) {
      const path = category === 'before' ? file.previousName : file.name
      diffHunkRanges[category].forEach((diffHunkRange) => {
        fileContentPair[category].tokens.forEach((token) => {
          const startPosition = getPositionFromIndex(
            fileContentPair[category].text,
            token.start,
          )
          const endPosition = getPositionFromIndex(
            fileContentPair[category].text,
            token.end,
          )
          const tokenRange = new Range(
            startPosition.lineNumber,
            startPosition.column,
            endPosition.lineNumber,
            endPosition.column + 1,
          )
          if (diffHunkRange.intersectRanges(tokenRange)) {
            diffHunkTokens[category].push({
              token: new TokenDetail(token, path, startPosition, endPosition),
              isCommonToken: false,
            })
          }
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
  const detectedCommonTokensArray: TokenDetailList[] = []
  function addDetectedCommonTokens(tokens: TokenDetailList) {
    if (detectedCommonTokensArray.some((t) => t.matches(tokens))) return
    detectedCommonTokensArray.push(tokens)
  }

  function registerCommonTokens(beforeI: number, afterI: number) {
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
    addDetectedCommonTokens(new TokenDetailList(commonTokenList))
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
        if (preScore >= 2) registerCommonTokens(beforeI - 1, afterI - 1)
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
  for (const tokens of detectedCommonTokensArray) {
    detectedCommonTokens.push(...tokens.tokenList, 'sequenceSeparator')
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
      commonTokensSetMap.add(
        new CommonTokens(new TokenDetailList(commonTokenList), category),
      )
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

const currentDecorations: {
  [category in DiffCategory]: Set<{
    commonTokens: CommonTokens
    isHovered: boolean
    metadata: DecorationMetadata
  }>
} = {
  before: new Set(),
  after: new Set(),
}

export function setCommonTokensDecorationOnEditor(
  path: string,
  category: DiffCategory,
  editor: monaco.editor.ICodeEditor,
  mousePosition?: [DiffCategory, monaco.Position],
) {
  const model = editor.getModel()
  if (!model) return
  const commonTokensList = commonTokensSetMap.getCommonTokensListIn(
    path,
    category,
  )
  for (const commonTokens of commonTokensList) {
    const { decoration, isHovered } = createCommonTokensDecoration(
      commonTokens,
      mousePosition,
    )
    const [id] = editor.deltaDecorations([], [decoration])
    currentDecorations[category].add({
      commonTokens,
      isHovered,
      metadata: {
        id,
        uri: model.uri.toString(),
      },
    })
  }
}
export function updateCommonTokensDecorationOnEditor(
  category: DiffCategory,
  editor: monaco.editor.ICodeEditor,
  mousePosition?: [DiffCategory, monaco.Position],
) {
  const model = editor.getModel()
  if (!model) return
  for (const decoration of [...currentDecorations[category]]) {
    const { commonTokens, isHovered, metadata } = decoration
    if (!mousePosition && !isHovered) continue
    const { decoration: updatedDecoration, isHovered: updatedIsHovered } =
      createCommonTokensDecoration(commonTokens, mousePosition)
    if (isHovered === updatedIsHovered) continue
    const [id] = editor.deltaDecorations([metadata.id], [updatedDecoration])
    currentDecorations[category].delete(decoration)
    currentDecorations[category].add({
      commonTokens,
      isHovered: updatedIsHovered,
      metadata: {
        id,
        uri: model.uri.toString(),
      },
    })
  }
}

export function clearCommonTokensDecorations(category: DiffCategory) {
  currentDecorations[category].forEach(({ metadata }) => {
    const model = monaco.editor.getModel(monaco.Uri.parse(metadata.uri))
    if (model) model.deltaDecorations([metadata.id], [])
  })
  currentDecorations[category].clear()
}

export function getCommonTokensSetOf(commonTokensRaw: string) {
  return commonTokensSetMap.getCommonTokensSetWithRaws(commonTokensRaw)
}

function createCommonTokensDecoration(
  commonTokens: CommonTokens,
  mousePosition?: [DiffCategory, monaco.Position],
): { decoration: monaco.editor.IModelDeltaDecoration; isHovered: boolean } {
  const commonTokensSet = commonTokensSetMap.getCommonTokensSet(
    commonTokens.tokens,
  )
  const commonTokensSetId = commonTokensSetMap.getCommonTokensSetId(
    commonTokens.tokens,
  )
  const hoverMessage = [
    {
      value: `**View Common Tokens: \`${commonTokens.tokens.joinedRaw}\`**`,
    },
    {
      value: `(total: ${commonTokensSet.size}, before: ${
        [...commonTokensSet].filter((c) => c.category === 'before').length
      }, after: ${
        [...commonTokensSet].filter((c) => c.category === 'after').length
      })`,
    },
  ]
  const count = commonTokensSet.size
  const level = count < 3 ? 1 : 2
  const isHoveredCommonTokens =
    !!mousePosition &&
    [...commonTokensSet].some(
      (c) =>
        c.category === mousePosition[0] &&
        c.range.containsPosition(mousePosition[1]),
    )
  const className =
    level !== 1
      ? null
      : isHoveredCommonTokens
      ? `commonTokens-decoration-hovered commonTokens-decoration-hovered-${level}`
      : `commonTokens-decoration commonTokens-decoration-${level}-${
          commonTokensSetId % 10
        }`
  return {
    decoration: {
      range: commonTokens.range,
      options: {
        className,
        hoverMessage,
      },
    },
    isHovered: isHoveredCommonTokens,
  }
}

/**
 * @param text the entire text of the file
 * @param index 0-indexed
 */
function getPositionFromIndex(text: string, index: number): Position {
  const raws = text.split('\n')
  for (let i = 0; i < raws.length - 1; i++) raws[i] += '\n'
  let indexCount = 0
  for (let i = 0; i < raws.length; i++) {
    const raw = raws[i]
    if (indexCount <= index && index < indexCount + raw.length) {
      const line = i + 1
      const column = index - indexCount + 1
      return new Position(line, column)
    }
    indexCount += raw.length
  }
  throw new Error(`index ${index} must be less than text length ${text.length}`)
}
function getCommitFileUri(
  owner: string,
  repository: string,
  sha: string,
  path: string,
) {
  return `https://github.com/${owner}/${repository}/blob/${sha}/${path}`
}

export class CommonTokenSequenceDecorationManager {
  private readonly currentDecorations: {
    [category in DiffCategory]: Set<{
      sequence: TokenSequence
      metadata: DecorationMetadata
    }>
  } = {
    before: new Set(),
    after: new Set(),
  }

  public setCommonTokensDecorations(
    path: string,
    category: DiffCategory,
    editor: monaco.editor.ICodeEditor,
  ) {
    const model = editor.getModel()
    if (!model) return
    const commonTokenSequences =
      useCommonTokenSequence().getCommonTokenSequencesIn(path, category)
    for (const { sequence, id, count } of commonTokenSequences) {
      const decoration = this.createCommonTokenSequenceDecoration(
        sequence,
        id,
        count,
      )
      const [decorationId] = editor.deltaDecorations([], [decoration])
      this.currentDecorations[category].add({
        sequence,
        metadata: {
          id: decorationId,
          uri: model.uri.toString(),
        },
      })
    }
  }

  public clearCommonTokensDecorations(category: DiffCategory) {
    this.currentDecorations[category].forEach(({ metadata }) => {
      const model = monaco.editor.getModel(monaco.Uri.parse(metadata.uri))
      if (model) model.deltaDecorations([metadata.id], [])
    })
    this.currentDecorations[category].clear()
  }

  private createCommonTokenSequenceDecoration(
    sequence: TokenSequence,
    id: number,
    count: { [category in DiffCategory]: number },
  ): monaco.editor.IModelDeltaDecoration {
    const hoverMessage = [
      {
        value: `**View Common Token Sequence (id=${id}): \`${sequence.joinedRaw}\` (before: ${count.before}, after: ${count.after})**`,
      },
    ]
    const className = `commonTokenSequence-decoration-${id}`
    return {
      range: sequence.range,
      options: {
        className,
        hoverMessage,
      },
    }
  }
}
