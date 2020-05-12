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
} from 'refactorhub'

export class Client {
  private $axios: NuxtAxiosInstance

  constructor($axios: NuxtAxiosInstance) {
    this.$axios = $axios
  }

  async getHello() {
    return (await this.$axios.get<string>('/api/hello')).data
  }

  async postHello() {
    return (await this.$axios.post<string>('/api/hello')).data
  }

  async getDraft(id: number) {
    return (await this.$axios.get<Draft>(`/api/draft/${id}`)).data
  }

  async updateDraft(id: number, data: { description?: string; type?: string }) {
    return (await this.$axios.patch<Draft>(`/api/draft/${id}`, data)).data
  }

  async saveDraft(id: number) {
    return (await this.$axios.post<Refactoring>(`/api/draft/${id}/save`)).data
  }

  async cancelDraft(id: number) {
    return (await this.$axios.post<void>(`/api/draft/${id}/cancel`)).data
  }

  async updateElement(
    id: number,
    category: DiffCategory,
    key: string,
    index: number,
    element: Element
  ) {
    return (
      await this.$axios.patch<Draft>(
        `/api/draft/${id}/${category}/${key}/${index}`,
        {
          element,
        }
      )
    ).data
  }

  async addElementLocation(id: number, category: DiffCategory, key: string) {
    return (await this.$axios.put<Draft>(`/api/draft/${id}/${category}/${key}`))
      .data
  }

  async addElementKey(
    id: number,
    category: DiffCategory,
    key: string,
    type: string,
    multiple: boolean
  ) {
    return (
      await this.$axios.put<Draft>(`/api/draft/${id}/${category}`, {
        key,
        type,
        multiple,
      })
    ).data
  }

  async getCommitInfo(sha: string) {
    return (await this.$axios.get<CommitInfo>(`/api/commit/${sha}/info`)).data
  }

  async getFileContent(
    owner: string,
    repository: string,
    sha: string,
    path: string
  ) {
    // TODO: text_model -> content
    return (
      await this.$axios.get<FileContent>(`/api/editor/text_model`, {
        params: { owner, repository, sha, path },
      })
    ).data
  }

  async getRefactorings() {
    return (await this.$axios.get<Refactoring[]>('/api/refactoring')).data
  }

  async forkRefactoring(id: number) {
    return (await this.$axios.post<Draft>(`/api/refactoring/${id}/fork`)).data
  }

  async editRefactoring(id: number) {
    return (await this.$axios.post<Draft>(`/api/refactoring/${id}/edit`)).data
  }

  async getRefactoringTypes() {
    return (await this.$axios.get<RefactoringType[]>('/api/refactoring/types'))
      .data
  }

  async getElementTypes() {
    return (await this.$axios.get<string[]>('/api/element/types')).data
  }

  async getUserRefactorings(id: number) {
    return (
      await this.$axios.get<Refactoring[]>(`/api/user/${id}/refactorings`)
    ).data
  }

  async getUserDrafts(id: number) {
    return (await this.$axios.get<Draft[]>(`/api/user/${id}/drafts`)).data
  }
}

const client: Plugin = ({ $axios }, inject) => {
  inject('client', new Client($axios))
}

export default client
