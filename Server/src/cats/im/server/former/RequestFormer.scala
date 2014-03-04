package cats.im.server.former

import cats.net.core.data.former.DataFormer
import cats.im.server.Opcodes
import cats.net.core.data.former.DataFormer.Former
import cats.net.core.data.Data
import cats.im.server.profile.Profile.Request

class RequestFormer extends DataFormer{

  def getOpcodes = Array(Opcodes.REQUEST)

  @Former def form(request: Request): Data = {
    data.put("type", request._type)
        .put("from", request._from)
        .put("to", request._to)
  }
}
