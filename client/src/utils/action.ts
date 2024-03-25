import apis, { ActionName, ActionType } from '@/apis'
import { logger } from '@/utils/logger'

export function sendAction(
  name: ActionName,
  data: object = {},
  type: ActionType = ActionType.Client,
) {
  apis.actions
    .postAction({
      name,
      type,
      data,
    })
    .catch((e) => {
      logger.warn(e)
    })
}
