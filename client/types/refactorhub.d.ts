declare module 'refactorhub' {
  /*
   * types for API
   */

  interface Refactoring {
    id: number
    ownerId: number
    parentId?: number
    commit: Commit
    type: string
    data: RefactoringData
    description: string
  }

  interface RefactoringData {
    before: { [key: string]: CodeElementHolder }
    after: { [key: string]: CodeElementHolder }
  }

  interface RefactoringType {
    name: string
    before: { [key: string]: CodeElementMetadata }
    after: { [key: string]: CodeElementMetadata }
    description: string
    id: number
  }

  interface Draft {
    id: number
    ownerId: number
    originId: number
    isFork: boolean
    commit: Commit
    type: string
    data: RefactoringData
    description: string
  }

  interface Element {
    type: string
    location: Location
  }

  interface CodeElementMetadata {
    type: string
    multiple: boolean
    required: boolean
  }

  interface CodeElementHolder {
    type: string
    multiple: boolean
    elements: Element[]
  }

  interface Location {
    path: string
    range?: Range
  }

  interface Range {
    startLine: number
    startColumn: number
    endLine: number
    endColumn: number
  }

  interface Commit {
    sha: string
    owner: string
    repository: string
  }

  interface CommitInfo {
    sha: string
    owner: string
    repository: string
    url: string
    message: string
    author: string
    authorDate: string
    files: CommitFile[]
    parent: string
  }

  interface CommitFile {
    sha: string
    status: CommitFileStatus
    name: string
    previousName: string
  }

  type CommitFileStatus = 'modified' | 'added' | 'removed' | 'renamed'

  interface User {
    id: number
    name: string
  }

  interface FileContent {
    value: string
    language?: string
    uri: string
    elements: Element[]
  }

  /*
   * types for Clients
   */

  type DiffCategory = 'before' | 'after'

  interface FileMetadata {
    index: number
  }

  interface ElementMetadata {
    key: string
    index: number
    type: string
  }

  interface DecorationMetadata {
    id: string
    uri: string
  }
}
