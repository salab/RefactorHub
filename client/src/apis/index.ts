import {
  Configuration,
  UsersApi,
  CommitsApi,
  RefactoringsApi,
  RefactoringTypesApi,
  DraftsApi,
  EditorApi,
  ElementsApi,
} from '@/apis/generated'

const config = new Configuration({
  basePath: '/api',
  credentials: 'same-origin',
})

const apis = {
  users: new UsersApi(config),
  commits: new CommitsApi(config),
  refactorings: new RefactoringsApi(config),
  refactoringTypes: new RefactoringTypesApi(config),
  drafts: new DraftsApi(config),
  editor: new EditorApi(config),
  elements: new ElementsApi(config),
}

export default apis
export * from '@/apis/generated/models'
