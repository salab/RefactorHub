import { User } from '@/apis/generated/api'
import { computed, useState } from '#imports'

export const useUser = () => {
  const user = useState<User | undefined>('user', () => undefined)
  const isAuthenticated = computed(() => user.value !== undefined)
  return {
    user,
    isAuthenticated,
  }
}
