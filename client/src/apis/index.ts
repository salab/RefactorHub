import axios from 'axios'
import {
  UsersApi,
  AnnotationsApi,
  AnnotationsSnapshotsApi,
  AnnotationsSnapshotsChangesApi,
  AnnotationsSnapshotsChangesParametersApi,
  ChangeTypesApi,
  CodeElementTypesApi,
  ExperimentsApi,
  ActionsApi,
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
  annotations: new AnnotationsApi(configuration, basePath, instance),
  snapshots: new AnnotationsSnapshotsApi(configuration, basePath, instance),
  changes: new AnnotationsSnapshotsChangesApi(
    configuration,
    basePath,
    instance,
  ),
  parameters: new AnnotationsSnapshotsChangesParametersApi(
    configuration,
    basePath,
    instance,
  ),
  changeTypes: new ChangeTypesApi(configuration, basePath, instance),
  codeElementTypes: new CodeElementTypesApi(configuration, basePath, instance),
  experiments: new ExperimentsApi(configuration, basePath, instance),
  actions: new ActionsApi(configuration, basePath, instance),
}

export default apis
export * from '@/apis/generated'
