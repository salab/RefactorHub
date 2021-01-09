import apis, { LogEvent, LogType } from '@/apis'
import { logger } from '@/utils/logger'

export const log = async (
  event: LogEvent,
  type: LogType,
  data: object = {}
) => {
  try {
    await apis.logs.postLog({
      event,
      type,
      data,
    })
  } catch (e) {
    logger.log(e)
  }
}
