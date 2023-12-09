import { Experiment } from '@/apis/generated/api'
import { useState } from '#imports'
import apis from '@/apis'

export const useExperiment = () => {
  const experimentMap = useState<Map<string, Experiment>>(
    'experimentMap',
    () => new Map(),
  )
  const gotAll = useState<boolean>('gotAllExperiment', () => false)

  async function getExperiment(experimentId: string) {
    const cachedExperiment = experimentMap.value.get(experimentId)
    if (cachedExperiment) return cachedExperiment
    const experiment = (await apis.experiments.getExperiment(experimentId)).data
    experimentMap.value.set(experimentId, experiment)
    return experiment
  }

  return {
    experimentMap,
    gotAll,
    getExperiment,
  }
}
