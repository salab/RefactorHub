import { Context } from '@nuxt/types'
import { NuxtAxiosInstance } from '@nuxtjs/axios'
import {
  Draft,
  Refactoring,
  CommitInfo,
  RefactoringType,
  TextModel
} from 'refactorhub'

export class Client {
  private $axios!: NuxtAxiosInstance

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

  async updateDraft(id: number, description?: string, type?: string) {
    return (await this.$axios.patch<Draft>(`/api/draft/${id}`, {
      description,
      type
    })).data
  }

  async getCommitInfo(sha: string) {
    return (await this.$axios.get<CommitInfo>(`/api/commit/${sha}/info`)).data
  }

  async getTextModel(
    owner: string,
    repository: string,
    sha: string,
    path: string
  ) {
    return (await this.$axios.get<TextModel>(`/api/editor/text_model`, {
      params: { owner, repository, sha, path }
    })).data
  }

  async getRefactorings() {
    return (await this.$axios.get<Refactoring[]>('/api/refactoring')).data
  }

  async forkRefactoring(id: number) {
    return (await this.$axios.post<Draft>(`/api/refactoring/${id}/fork`)).data
  }

  async getRefactoringTypes() {
    return (await this.$axios.get<RefactoringType[]>('/api/refactoring/types'))
      .data
  }

  async getElementTypes() {
    return (await this.$axios.get<string[]>('/api/element/types')).data
  }
}

export default (
  { $axios }: Context,
  inject: (name: string, client: Client) => void
) => {
  inject('client', new Client($axios))
}
