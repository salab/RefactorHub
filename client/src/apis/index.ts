import axios from 'axios'
import {
  UsersApi,
  CommitsApi,
  RefactoringsApi,
  RefactoringTypesApi,
  DraftsApi,
  EditorApi,
  ElementsApi,
  ExperimentsApi,
  LogsApi,
  Configuration,
} from '@/apis/generated'

const configuration = new Configuration()
const basePath = '/api'
const instance = axios.create({
  withCredentials: true,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
})

const apis = {
  users: new UsersApi(configuration, basePath, instance),
  commits: new CommitsApi(configuration, basePath, instance),
  refactorings: new RefactoringsApi(configuration, basePath, instance),
  refactoringTypes: new RefactoringTypesApi(configuration, basePath, instance),
  drafts: new DraftsApi(configuration, basePath, instance),
  editor: new EditorApi(configuration, basePath, instance),
  elements: new ElementsApi(configuration, basePath, instance),
  experiments: new ExperimentsApi(configuration, basePath, instance),
  logs: new LogsApi(configuration, basePath, instance),
}

export default apis
export * from '@/apis/generated'
