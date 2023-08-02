import { consola } from 'consola'

const isDevelopment = process.env.NODE_ENV === 'development'

export const logger = consola.create({
  level: isDevelopment ? 4 : 0,
})
