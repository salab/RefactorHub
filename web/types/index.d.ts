export interface Annotation {
  owner: User
  commit: Commit
  parent: Annotation | null
  type: RefactoringType
  refactoring: Refactoring
  description: string
  created: Date
  lastModified: Date
  id: number
}

export interface Draft {
  owner: User
  commit: Commit
  parent: Annotation | null
  origin: Annotation | null
  type: RefactoringType
  refactoring: Refactoring
  description: string
  id: number
}

export interface Refactoring {
  type: RefactoringType
  before: ElementSet
  after: ElementSet
}

export enum RefactoringType {
  CUSTOM = 'Custom'
}

export interface ElementSet {
  custom: { [key: string]: Element }
}

export interface Element {
  type: ElementType
  location: Location
}

export enum ElementType {}

export interface Location {
  path: string
  range: Range
}

export interface Range {
  startLine: number
  startColumn: number
  endLine: number
  endColumn: number
}

export interface Commit {
  sha: string
  owner: string
  repository: string
}

export interface CommitInfo {
  sha: string
  owner: string
  repository: string
  url: string
  message: string
  author: string
  authorDate: Date
  files: CommitFile[]
}

export interface CommitFile {
  sha: string
  status: CommitFileStatus
  name: string
  previousName: string
}

export enum CommitFileStatus {
  MODIFIED = 'modified',
  ADDED = 'added',
  REMOVED = 'removed',
  RENAMED = 'renamed'
}

export interface User {
  id: number
  name: string
}
