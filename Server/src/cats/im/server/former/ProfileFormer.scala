package cats.im.server.former

import cats.net.core.data.former.DataFormer
import cats.im.server.Opcodes
import cats.net.core.data.former.DataFormer.Former
import cats.im.server.profile.Profile
import cats.net.core.data.Data

class ProfileFormer extends DataFormer{

  def getOpcodes = Array(Opcodes.ADD_FRIEND, Opcodes.DELETE_FRIEND, Opcodes.INIT)

  @Former def form(prof: Profile): Data = {
    data.put("login", prof.login)
    if(data.opcode == Opcodes.DELETE_FRIEND)
      return data
    data.put("name", prof.name)
        .put("access", prof.access)
        .put("status", prof.status)
        .put("mood", prof.mood)
        .put("picUrl", prof.picUrl)
  }
  
  @Former def form(login: String): Data = data.put("login", login)
}
