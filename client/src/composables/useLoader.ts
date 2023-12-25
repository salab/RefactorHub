export const useLoader = () => {
  const isLoading = useState('isLoading', () => false)
  const loadingText = useState('loadingText', () => '')

  function setLoadingText(text: string) {
    loadingText.value = text
  }

  function startLoading(annotationId: string): Promise<void> {
    isLoading.value = true
    initialize()

    // start loading asynchronously
    loadAnnotation(annotationId).then(() => {
      isLoading.value = false
      setLoadingText('')
    })
    return Promise.resolve() // loading is started
  }

  function initialize() {
    setLoadingText('initializing')
    useAnnotation().initialize()
    useParameter().initialize()
    useCommonTokenSequence().initialize()
    useViewer().initialize()
  }

  async function loadAnnotation(annotationId: string) {
    const initialFilePair = await useAnnotation().setup(annotationId)
    useCommonTokenSequence().setup()
    useViewer().setup(initialFilePair)
  }

  return {
    isLoading: computed(() => isLoading.value),
    loadingText: computed(() => loadingText.value),
    setLoadingText,
    startLoading,
  }
}
