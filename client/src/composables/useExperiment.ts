import { Experiment } from '@/apis/generated/api'
import { useState } from '#imports'

export const useExperiment = () => {
  const experimentMap = useState<Map<string, Experiment>>(
    'experimentMap',
    () => new Map(),
  )
  const gotAll = useState<boolean>('gotAllExperiment', () => false)
  return {
    experimentMap,
    gotAll,
  }
}
