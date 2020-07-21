import { Plugin } from '@nuxt/types'
import { NuxtAxiosInstance } from '@nuxtjs/axios'
import {
  Element,
  Draft,
  Refactoring,
  CommitInfo,
  RefactoringType,
  FileContent,
  DiffCategory,
  CodeElementMetadata,
  Commit,
  RefactoringData,
} from 'refactorhub'

export class Client {
  private $axios: NuxtAxiosInstance

  constructor($axios: NuxtAxiosInstance) {
    this.$axios = $axios
  }

  async getUserRefactorings(id: number) {
    return (
      await this.$axios.get<Refactoring[]>(`/api/users/${id}/refactorings`)
    ).data
  }

  async getUserDrafts(id: number) {
    return (await this.$axios.get<Draft[]>(`/api/users/${id}/drafts`)).data
  }

  async getCommitDetail(sha: string) {
    return (await this.$axios.get<CommitInfo>(`/api/commits/${sha}/detail`))
      .data
  }

  async createRefactoring(
    type: string,
    description: string,
    commit: Commit,
    data: RefactoringData
  ) {
    return (
      await this.$axios.post<Refactoring>('/api/refactorings', {
        type,
        description,
        commit,
        data,
      })
    ).data
  }

  async getRefactorings() {
    return (await this.$axios.get<Refactoring[]>('/api/refactorings')).data
  }

  async getRefactoringChildren(id: number) {
    return (
      await this.$axios.get<Refactoring[]>(`/api/refactorings/${id}/children`)
    ).data
  }

  async getRefactoringDrafts(id: number) {
    return (await this.$axios.get<Draft[]>(`/api/refactorings/${id}/drafts`))
      .data
  }

  async forkRefactoring(id: number) {
    return (await this.$axios.post<Draft>(`/api/refactorings/${id}/fork`)).data
  }

  async editRefactoring(id: number) {
    return (await this.$axios.post<Draft>(`/api/refactorings/${id}/edit`)).data
  }

  async getRefactoringTypes() {
    return (await this.$axios.get<RefactoringType[]>('/api/refactoring_types'))
      .data
  }

  async getRefactoringType(name: string) {
    return (
      await this.$axios.get<RefactoringType>(`/api/refactoring_types/${name}`)
    ).data
  }

  async createRefactoringType(
    name: string,
    before: { [key: string]: CodeElementMetadata },
    after: { [key: string]: CodeElementMetadata }
  ) {
    return (
      await this.$axios.post<RefactoringType>('/api/refactoring_types', {
        name,
        before,
        after,
      })
    ).data
  }

  async getDraft(id: number) {
    return (await this.$axios.get<Draft>(`/api/drafts/${id}`)).data
  }

  async updateDraft(id: number, data: { description?: string; type?: string }) {
    return (await this.$axios.patch<Draft>(`/api/drafts/${id}`, data)).data
  }

  async saveDraft(id: number) {
    return (await this.$axios.post<Refactoring>(`/api/drafts/${id}/save`)).data
  }

  async discardDraft(id: number) {
    return (await this.$axios.post<void>(`/api/drafts/${id}/discard`)).data
  }

  async putElementKey(
    id: number,
    category: DiffCategory,
    key: string,
    type: string,
    multiple: boolean
  ) {
    return (
      await this.$axios.put<Draft>(`/api/drafts/${id}/${category}`, {
        key,
        type,
        multiple,
      })
    ).data
  }

  async removeElementKey(id: number, category: DiffCategory, key: string) {
    return (
      await this.$axios.delete<Draft>(`/api/drafts/${id}/${category}/${key}`)
    ).data
  }

  async appendElementValue(id: number, category: DiffCategory, key: string) {
    return (
      await this.$axios.post<Draft>(`/api/drafts/${id}/${category}/${key}`)
    ).data
  }

  async updateElementValue(
    id: number,
    category: DiffCategory,
    key: string,
    index: number,
    element: Element
  ) {
    return (
      await this.$axios.patch<Draft>(
        `/api/drafts/${id}/${category}/${key}/${index}`,
        {
          element,
        }
      )
    ).data
  }

  async removeElementValue(
    id: number,
    category: DiffCategory,
    key: string,
    index: number
  ) {
    return (
      await this.$axios.delete<Draft>(
        `/api/drafts/${id}/${category}/${key}/${index}`
      )
    ).data
  }

  async getFileContent(
    owner: string,
    repository: string,
    sha: string,
    path: string
  ) {
    return (
      await this.$axios.get<FileContent>(`/api/editor/content`, {
        params: { owner, repository, sha, path },
      })
    ).data
  }

  async getElementTypes() {
    return (await this.$axios.get<string[]>('/api/elements/types')).data
  }
}

const client: Plugin = ({ $axios }, inject) => {
  inject('client', new Client($axios))
}

export default client
