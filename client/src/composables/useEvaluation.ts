import { useState } from '#imports'

export const useEvaluation = () => {
  const isBaseline = useState<boolean | undefined>(
    'isBaseline',
    () => undefined,
  )

  function initialize() {
    isBaseline.value = undefined
  }

  async function setup() {
    useLoader().setLoadingText('fetching experiment information')
    const experimentId = useAnnotation().annotation.value?.experimentId
    if (!experimentId)
      throw new Error(
        'setup of useEvaluation must be executed after setup of useAnnotation',
      )
    const experiment = await useExperiment().get(experimentId)

    // HARD CODING
    isBaseline.value = experiment.title.includes('(P)')

    if (isBaseline.value) {
      useCommonTokenSequence().updateSetting({
        oneToOne: false,
        oneToManyOrManyToOne: false,
        manyToMany: false,
      })
    }
  }

  return { isBaseline: computed(() => isBaseline.value), initialize, setup }
}
