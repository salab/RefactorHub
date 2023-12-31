export const useLoader = () => {
  const loadingText = useState<string | undefined>(
    'loadingText',
    () => undefined,
  )

  function setLoadingText(text: string) {
    loadingText.value = text
  }
  function finishLoading() {
    loadingText.value = undefined
  }

  function startLoadingAnnotation(annotationId: string): Promise<void> {
    setLoadingText('initializing')
    initialize()

    // start loading asynchronously
    loadAnnotation(annotationId).then(() => {
      finishLoading()
    })
    return Promise.resolve() // loading is started
  }

  function initialize() {
    useAnnotation().initialize()
    useParameter().initialize()
    useCommonTokenSequence().initialize()
    useViewer().initialize()
    useEvaluation().initialize()
  }

  async function loadAnnotation(annotationId: string) {
    const initialFilePair = await useAnnotation().setup(annotationId)
    useCommonTokenSequence().setup()
    useViewer().setup(initialFilePair)
    useEvaluation().setup()
  }

  return {
    isLoading: computed(() => loadingText.value !== undefined),
    loadingText: computed(() => loadingText.value),
    startLoadingAnnotation,
    setLoadingText,
    finishLoading,
  }
}
