package cats.im.server.former

import cats.net.core.data.former.DataFormer
import cats.im.server.Opcodes
import cats.net.core.data.former.DataFormer.Former
import cats.net.core.data.Data
import cats.im.server.profile.Profile

class RequestResponseFormer extends DataFormer{

  def getOpcodes = Array(Opcodes.REQUEST_RESPONSE)

  @Former def form(request: Profile.Request, response: Byte): Data = {
    form(request.id, response)
  }

  @Former def form(id: Long, response: Byte): Data = {
    data.put("id", id).put("response", response)
  }

}
