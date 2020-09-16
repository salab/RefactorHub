import consola, { LogLevel } from 'consola'

consola.level =
  process.env.NODE_ENV === 'development' ? LogLevel.Debug : LogLevel.Error
