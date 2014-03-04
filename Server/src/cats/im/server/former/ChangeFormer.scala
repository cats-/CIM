package cats.im.server.former

import cats.net.core.data.former.DataFormer
import cats.im.server.Opcodes
import cats.net.core.data.former.DataFormer.Former
import cats.im.server.profile.Profile
import cats.net.core.data.Data

class ChangeFormer extends DataFormer{

  def getOpcodes = Array(Opcodes.CHANGE_MOOD, Opcodes.CHANGE_NAME, Opcodes.CHANGE_STATUS, Opcodes.CHANGE_PIC, Opcodes.CHANGE_ACCESS)

  @Former def form(prof: Profile, arg: Any): Data = {
    form(prof.login, arg)
  }

  @Former def form(login: String, arg: Any): Data = {
    data.put("login", login).put("arg", arg)
  }

  @Former def form(prof: Profile): Data = {
    form(prof, data.opcode match {
      case Opcodes.CHANGE_NAME => prof.name
      case Opcodes.CHANGE_MOOD => prof.mood
      case Opcodes.CHANGE_STATUS => prof.status
      case Opcodes.CHANGE_PIC => prof.picUrl
      case Opcodes.CHANGE_ACCESS => prof.access
    })
  }
}
