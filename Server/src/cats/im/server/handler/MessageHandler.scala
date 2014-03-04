package cats.im.server.handler

import cats.im.server.{Opcodes, Server}
import cats.net.server.handler.ServerDataHandler
import cats.net.server.ClientConnection
import cats.net.core.data.Data
import cats.im.server.profile.{Profile, ProfileManager}

class MessageHandler extends ServerDataHandler[Server]{

  def getOpcodes = Array(Opcodes.MSG)

  def handle(server: Server, con: ClientConnection, data: Data){
    val from: Profile = ProfileManager get con
    val toLogin: String = data getString "to"
    val to: Profile = ProfileManager get toLogin
    if(to == null){
      con.send(Opcodes.POPUP_MSG, s"$toLogin does not exist")
      return
    }
    if(!from.friends.contains(to)){
      con.send(Opcodes.POPUP_MSG, s"Unable to send msg to $toLogin. reason: not friends")
      return
    }
    from.send(Opcodes.MSG, from, to)
    to.send(Opcodes.MSG, from, to)
  }

}
