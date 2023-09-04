export const useLoader = () => {
  const isLoading = useState('isLoading', () => false)
  const loadingText = useState('loadingText', () => '')

  function setLoadingText(text: string) {
    loadingText.value = text
  }

  function startLoading(draftId: number): Promise<void> {
    isLoading.value = true
    initialize()

    // start loading asynchronously
    loadDraft(draftId).then(() => {
      isLoading.value = false
      setLoadingText('')
    })
    return Promise.resolve() // loading is started
  }

  function initialize() {
    setLoadingText('initializing')
    useDraft().initialize()
    useCommonTokenSequence().initialize()
    useViewer().initialize()
  }

  async function loadDraft(draftId: number) {
    const commit = await useDraft().setup(draftId)
    await useCommonTokenSequence().setup(commit)
    useViewer().setup(commit)
  }

  return {
    isLoading: computed(() => isLoading.value),
    loadingText: computed(() => loadingText.value),
    setLoadingText,
    startLoading,
  }
}
