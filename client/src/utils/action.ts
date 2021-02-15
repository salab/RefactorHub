import apis, { ActionName, ActionType } from '@/apis'
import { logger } from '@/utils/logger'

export const log = async (
  name: ActionName,
  type: ActionType,
  data: object = {}
) => {
  try {
    await apis.actions.postAction({
      name,
      type,
      data,
    })
  } catch (e) {
    logger.log(e)
  }
}
