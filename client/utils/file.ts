export function readAsText(file?: File) {
  if (!file) return Promise.resolve('')
  const reader = new FileReader()
  return new Promise<string>((resolve) => {
    reader.addEventListener('loadend', (e: ProgressEvent<FileReader>) => {
      const result = e?.target?.result?.toString() || ''
      resolve(result)
    })
    reader.readAsText(file)
  })
}
