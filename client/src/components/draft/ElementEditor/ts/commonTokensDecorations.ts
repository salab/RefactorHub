import * as monaco from 'monaco-editor'
import { DecorationMetadata, DiffCategory } from 'refactorhub'
import { accessorType } from '@/store'
import { Token } from '@/apis'
import { Position, Range } from 'monaco-editor'

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
    endPosition: Position
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
export class CommonTokens {
  public readonly tokens: TokenDetail[]
  public readonly path: string
  public readonly category: DiffCategory
  public constructor(
    tokens: TokenDetail[],
    path: string,
    category: DiffCategory
  ) {
    this.tokens = tokens
    this.path = path
    this.category = category
  }

  public get range(): Range {
    const firstToken = this.tokens[0]
    const lastToken = this.tokens[this.tokens.length - 1]
    return new Range(
      firstToken.startPosition.lineNumber,
      firstToken.startPosition.column,
      lastToken.endPosition.lineNumber,
      lastToken.endPosition.column + 1
    )
  }

  public get tokensSubSequences(): Set<CommonTokens> {
    const tokens = this.tokens
    const resultSet = new Set<CommonTokens>()
    for (let i = 0; i < tokens.length; i++) {
      if (tokens[i].isSymbol) continue
      for (let j = i; j < tokens.length; j++) {
        if (tokens[j].isSymbol) continue
        resultSet.add(this.slice(i, j + 1))
      }
    }
    return resultSet
  }

  public get joinedRaw(): string {
    return this.tokens
      .map((token) => token.raw.replace('\r', '').replace('\n', ''))
      .join(' ')
  }

  public equals(other: CommonTokens): boolean {
    if (this.path !== other.path) return false
    if (this.category !== other.category) return false
    for (let i = 0; i < this.tokens.length; i++)
      if (!this.tokens[i].equals(other.tokens[i])) return false
    return true
  }

  public isIn(path: string, category: DiffCategory): boolean {
    return this.path === path && this.category === category
  }

  private slice(start?: number, end?: number): CommonTokens {
    return new CommonTokens(
      this.tokens.slice(start, end),
      this.path,
      this.category
    )
  }
}
class CommonTokensSetMap {
  private readonly commonTokensSetMap: {
    /* key */ tokens: TokenDetail[]
    /* value */ commonTokensSet: CommonTokens[]
  }[] = []

  /** the sha of analyzed commit */
  private _sha = ''

  public get sha() {
    return this._sha
  }

  public add(commonTokens: CommonTokens) {
    const tokensSubSequences = commonTokens.tokensSubSequences
    tokensSubSequences.forEach((commonTokens) =>
      this.addCommonTokens(commonTokens)
    )
  }

  public getCommonTokensListIn(
    path: string,
    category: DiffCategory
  ): CommonTokens[] {
    const results: CommonTokens[] = []
    for (const { commonTokensSet } of this.commonTokensSetMap) {
      for (const commonTokens of commonTokensSet) {
        if (commonTokens.isIn(path, category)) results.push(commonTokens)
      }
    }
    return results
  }

  public getCommonTokensSet(tokens: TokenDetail[]): Set<CommonTokens> {
    const commonTokensSetEntry = this.commonTokensSetMap.find((entry) =>
      this.tokensMatches(entry.tokens, tokens)
    )
    if (!commonTokensSetEntry) return new Set()
    return new Set(commonTokensSetEntry.commonTokensSet)
  }

  public getCommonTokensSetWithRaws(
    commonTokensRaw: string
  ): Set<CommonTokens> {
    const commonTokensSetEntry = this.commonTokensSetMap.find((entry) => {
      return (
        entry.tokens
          .map((t) => t.raw.replace('\r', '').replace('\n', ''))
          .join(' ') === commonTokensRaw
      )
    })
    if (!commonTokensSetEntry) return new Set()
    return new Set(commonTokensSetEntry.commonTokensSet)
  }

  public reset(sha: string) {
    this._sha = sha
    this.commonTokensSetMap.length = 0
  }

  private addCommonTokens(commonTokens: CommonTokens) {
    const tokens = commonTokens.tokens
    const commonTokensSetEntry = this.commonTokensSetMap.find((entry) =>
      this.tokensMatches(entry.tokens, tokens)
    )
    if (!commonTokensSetEntry) {
      this.commonTokensSetMap.push({ tokens, commonTokensSet: [commonTokens] })
      return
    }
    if (
      commonTokensSetEntry.commonTokensSet.every((c) => !c.equals(commonTokens))
    ) {
      commonTokensSetEntry.commonTokensSet.push(commonTokens)
    }
  }

  private tokensMatches(
    tokens1: TokenDetail[],
    tokens2: TokenDetail[]
  ): boolean {
    if (tokens1.length !== tokens2.length) return false
    for (let i = 0; i < tokens1.length; i++) {
      if (!tokens1[i].matches(tokens2[i])) return false
    }
    return true
  }
}

const commonTokensSetMap = new CommonTokensSetMap()

export async function initCommonTokensMap($accessor: typeof accessorType) {
  const commit = $accessor.draft.commit
  if (!commit) return
  const { owner, repository, files, sha } = commit
  if (sha === commonTokensSetMap.sha) return // no need for re-analyze
  const diffHunkTokens: {
    [category in DiffCategory]: (TokenDetail | 'diffHunkSeparator')[]
  } = {
    before: [],
    after: [],
  }
  for (const file of files) {
    const diffHunkRanges: { [category in DiffCategory]: Range[] } = {
      before: [],
      after: [],
    }
    file.diffHunks.forEach(({ before, after }) => {
      if (before)
        diffHunkRanges.before.push(
          new Range(before.startLine, 1, before.endLine + 1, 0)
        )
      if (after)
        diffHunkRanges.after.push(
          new Range(after.startLine, 1, after.endLine + 1, 0)
        )
    })
    const fileContentPair = {
      before: await $accessor.draft.getFileContent({
        owner,
        repository,
        sha: commit.sha,
        category: 'before',
        path: file.previousName,
        uri: getCommitFileUri(
          commit.owner,
          commit.repository,
          commit.parent,
          file.previousName
        ),
      }),
      after: await $accessor.draft.getFileContent({
        owner,
        repository,
        sha: commit.sha,
        category: 'after',
        path: file.name,
        uri: getCommitFileUri(
          commit.owner,
          commit.repository,
          commit.sha,
          file.name
        ),
      }),
    }
    function addDiffHunkTokens(category: DiffCategory) {
      const path = category === 'before' ? file.previousName : file.name
      diffHunkRanges[category].forEach((diffHunkRange) => {
        fileContentPair[category].tokens.forEach((token) => {
          const startPosition = getPositionFromIndex(
            fileContentPair[category].text,
            token.start
          )
          const endPosition = getPositionFromIndex(
            fileContentPair[category].text,
            token.end
          )
          const tokenRange = new Range(
            startPosition.lineNumber,
            startPosition.column,
            endPosition.lineNumber,
            endPosition.column + 1
          )
          if (diffHunkRange.intersectRanges(tokenRange)) {
            diffHunkTokens[category].push(
              new TokenDetail(token, path, startPosition, endPosition)
            )
          }
        })
        diffHunkTokens[category].push('diffHunkSeparator')
      })
    }
    addDiffHunkTokens('before')
    addDiffHunkTokens('after')
    diffHunkTokens.before.push('diffHunkSeparator')
    diffHunkTokens.after.push('diffHunkSeparator')
  }

  commonTokensSetMap.reset(sha)
  const chainScoreTable: number[][] = []
  function checkCommonTokens(beforeI: number, afterI: number) {
    const maxScore = chainScoreTable[beforeI][afterI]
    if (maxScore === 0) return
    let startIPair: { beforeI: number; afterI: number } | undefined
    let endIPair: { beforeI: number; afterI: number } | undefined
    let i = 1
    while (beforeI >= i && afterI >= i) {
      const score = chainScoreTable[beforeI - i][afterI - i]
      if (endIPair === undefined && maxScore !== score) {
        endIPair = { beforeI: beforeI - i + 1, afterI: afterI - i + 1 }
      }
      if (score === 0) {
        startIPair = { beforeI: beforeI - i + 1, afterI: afterI - i + 1 }
        break
      }
      i++
    }
    i = Math.min(beforeI, afterI)
    if (!endIPair) endIPair = { beforeI: beforeI - i, afterI: afterI - i }
    if (!startIPair) startIPair = { beforeI: beforeI - i, afterI: afterI - i }
    const tokensBefore = diffHunkTokens.before.slice(
      startIPair.beforeI,
      endIPair.beforeI + 1
    ) as TokenDetail[]
    const commonTokensBefore = new CommonTokens(
      tokensBefore,
      tokensBefore[0].path,
      'before'
    )
    commonTokensSetMap.add(commonTokensBefore)
    const tokensAfter = diffHunkTokens.after.slice(
      startIPair.afterI,
      endIPair.afterI + 1
    ) as TokenDetail[]
    const commonTokensAfter = new CommonTokens(
      tokensAfter,
      tokensAfter[0].path,
      'after'
    )
    commonTokensSetMap.add(commonTokensAfter)
  }
  for (let beforeI = 0; beforeI < diffHunkTokens.before.length; beforeI++) {
    chainScoreTable[beforeI] = []
    for (let afterI = 0; afterI < diffHunkTokens.after.length; afterI++) {
      const tokenBefore = diffHunkTokens.before[beforeI]
      const tokenAfter = diffHunkTokens.after[afterI]
      if (beforeI === 0 || afterI === 0) {
        if (
          tokenBefore === 'diffHunkSeparator' ||
          tokenAfter === 'diffHunkSeparator' ||
          !tokenBefore.matches(tokenAfter) ||
          tokenBefore.isSymbol
        ) {
          chainScoreTable[beforeI][afterI] = 0
          continue
        }
        chainScoreTable[beforeI][afterI] = 1
        continue
      }
      if (
        tokenBefore === 'diffHunkSeparator' ||
        tokenAfter === 'diffHunkSeparator' ||
        !tokenBefore.matches(tokenAfter)
      ) {
        checkCommonTokens(beforeI - 1, afterI - 1)
        chainScoreTable[beforeI][afterI] = 0
        continue
      }
      chainScoreTable[beforeI][afterI] =
        chainScoreTable[beforeI - 1][afterI - 1] +
        (!tokenBefore.isSymbol ? 1 : 0)
    }
  }
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
  mousePosition?: [DiffCategory, monaco.Position]
) {
  const model = editor.getModel()
  if (!model) return
  const commonTokensList = commonTokensSetMap.getCommonTokensListIn(
    path,
    category
  )
  for (const commonTokens of commonTokensList) {
    const { decoration, isHovered } = createCommonTokensDecoration(
      commonTokens,
      mousePosition
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
  mousePosition?: [DiffCategory, monaco.Position]
) {
  const model = editor.getModel()
  if (!model) return
  for (const decoration of [...currentDecorations[category]]) {
    const { commonTokens, isHovered, metadata } = decoration
    if (!mousePosition && !isHovered) continue
    const {
      decoration: updatedDecoration,
      isHovered: updatedIsHovered,
    } = createCommonTokensDecoration(commonTokens, mousePosition)
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
  mousePosition?: [DiffCategory, monaco.Position]
): { decoration: monaco.editor.IModelDeltaDecoration; isHovered: boolean } {
  const commonTokensSet = commonTokensSetMap.getCommonTokensSet(
    commonTokens.tokens
  )
  const hoverMessage = [
    {
      value: `**View Common Tokens: \`${commonTokens.joinedRaw}\`**`,
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
        c.range.containsPosition(mousePosition[1])
    )
  const className = isHoveredCommonTokens
    ? `commonTokens-decoration-hovered commonTokens-decoration-hovered-${level}`
    : `commonTokens-decoration commonTokens-decoration-${level}`
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
  path: string
) {
  return `https://github.com/${owner}/${repository}/blob/${sha}/${path}`
}
