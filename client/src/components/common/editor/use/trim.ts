export function trimFileName(name: string, length: number = 50): string {
  if (name.length > length) {
    return `...${name.substr(name.length - length + 3)}`
  }
  return name
}
