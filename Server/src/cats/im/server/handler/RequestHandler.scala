package cats.im.server.handler

import cats.im.server.{Server, Opcodes}
import cats.net.server.handler.ServerDataHandler
import cats.net.core.data.Data
import cats.net.server.ClientConnection
import cats.im.server.profile.{ProfileManager, Profile}

class RequestHandler extends ServerDataHandler[Server]{

  def getOpcodes = Array(Opcodes.REQUEST)

  def handle(server: Server, con: ClientConnection, data: Data){
    val prof: Profile = ProfileManager get con
    val reqType: Byte = data getByte "type"
    if(!Profile.Request.isOneOf(reqType)){
      con.send(Opcodes.POPUP_MSG, s"invalid request type ($reqType)")
      return
    }
    val otherLogin: String = data getString "to"
    val other: Profile = ProfileManager get otherLogin
    if(other == null){
      con.send(Opcodes.POPUP_MSG, s"$otherLogin does not exist")
      return
    }
    val request: Profile.Request = new Profile.Request(reqType, prof.login, otherLogin)
    prof addRequest request
    other addRequest request
    if(other.connected)
      other.send(Opcodes.REQUEST, request)
    ProfileManager.save()
  }

}
