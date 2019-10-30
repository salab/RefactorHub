declare module 'refactorhub' {
  interface Refactoring {
    owner: User
    commit: Commit
    parent: Refactoring | null
    type: RefactoringType
    data: RefactoringData
    description: string
    created: string
    lastModified: string
    id: number
  }

  interface RefactoringData {
    before: { [key: string]: Element }
    after: { [key: string]: Element }
  }

  interface RefactoringType {
    name: string
    before: { [key: string]: string }
    after: { [key: string]: string }
    description: string
    id: number
  }

  interface Draft {
    owner: User
    commit: Commit
    parent: Refactoring | null
    origin: Refactoring | null
    type: RefactoringType
    data: RefactoringData
    description: string
    id: number
  }

  interface Element {
    type: string
    location: Location
    incomplete: boolean
  }

  interface Location {
    path: string
    range: Range
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

  interface TextModel {
    value: string
    language?: string
    uri?: string
    elements: Element[]
  }

  type Diff = 'before' | 'after'
}
