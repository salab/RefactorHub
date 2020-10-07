import consola, { LogLevel } from 'consola'

const isDevelopment = process.env.NODE_ENV === 'development'

export const logger = consola.create({
  level: isDevelopment ? LogLevel.Debug : LogLevel.Error,
})
