import { Experiment } from '@/apis/generated/api'
import { useState } from '#imports'
import apis from '@/apis'

export const useExperiment = () => {
  const experimentMap = useState<Map<string, Experiment>>(
    'experimentMap',
    () => new Map(),
  )

  async function get(experimentId: string) {
    const cachedExperiment = experimentMap.value.get(experimentId)
    if (cachedExperiment) return cachedExperiment
    const experiment = (await apis.experiments.getExperiment(experimentId)).data
    experimentMap.value.set(experimentId, experiment)
    return experiment
  }

  async function getAll() {
    const experiments = (await apis.experiments.getExperiments()).data
    experimentMap.value.clear()
    experiments.forEach((experiment) => {
      experimentMap.value.set(experiment.id, experiment)
    })
    return [...experimentMap.value.values()]
  }

  async function create(data: {
    title: string
    description: string
    commits: { owner: string; repository: string; sha: string }[]
  }) {
    const experiment = (
      await apis.experiments.createExperiment({
        ...data,
      })
    ).data
    experimentMap.value.set(experiment.id, experiment)
    return [...experimentMap.value.values()]
  }

  return { get, getAll, create }
}
