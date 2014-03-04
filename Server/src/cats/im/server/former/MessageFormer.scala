package cats.im.server.former

import cats.net.core.data.former.DataFormer
import cats.net.core.data.former.DataFormer.Former
import cats.im.server.Opcodes
import cats.net.core.data.Data
import cats.im.server.profile.Profile

class MessageFormer extends DataFormer{

  def getOpcodes = Array(Opcodes.MSG)

  @Former def form(from: String, to: String, msg: String): Data = {
    data.put("from", from).put("to", to).put("msg", msg)
  }

  @Former def form(from: Profile, to: Profile, msg: String): Data = {
    form(from.login, to.login, msg)
  }

}
