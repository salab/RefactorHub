import axios from 'axios'
import {
  UsersApi,
  CommitsApi,
  RefactoringsApi,
  RefactoringTypesApi,
  DraftsApi,
  EditorApi,
  ElementsApi,
} from '@/apis/generated'

const basePath = '/api'
const instance = axios.create({
  withCredentials: true,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
})

instance.interceptors.response.use((response) => {
  if (response.status === 401) {
    location.href = '/login'
  }
  return response
})

const apis = {
  users: new UsersApi({}, basePath, instance),
  commits: new CommitsApi({}, basePath, instance),
  refactorings: new RefactoringsApi({}, basePath, instance),
  refactoringTypes: new RefactoringTypesApi({}, basePath, instance),
  drafts: new DraftsApi({}, basePath, instance),
  editor: new EditorApi({}, basePath, instance),
  elements: new ElementsApi({}, basePath, instance),
}

export default apis
export * from '@/apis/generated'
