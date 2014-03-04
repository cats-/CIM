package cats.im.server.former

import cats.net.core.data.former.DataFormer
import cats.im.server.Opcodes
import cats.net.core.data.former.DataFormer.Former
import cats.net.core.data.Data

class PopupFormer extends DataFormer{

  def getOpcodes = Array(Opcodes.POPUP_MSG)

  @Former def form(msg: String): Data = data.put("msg", msg)

}
