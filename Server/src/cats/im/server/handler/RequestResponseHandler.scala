package cats.im.server.handler

import cats.im.server.{Opcodes, Server}
import cats.net.server.handler.ServerDataHandler
import cats.net.core.data.Data
import cats.net.server.ClientConnection
import cats.im.server.profile.{Profile, ProfileManager}

class RequestResponseHandler extends ServerDataHandler[Server]{

  private val Cancel: Byte = -1
  private val No: Byte = 0
  private val Yes: Byte = 1

  def getOpcodes = Array(Opcodes.REQUEST_RESPONSE)

  def handle(server: Server, con: ClientConnection, data: Data){
    val prof: Profile = ProfileManager get con
    val id: Long = data getLong "id"
    val request: Profile.Request = prof getRequest id
    if(request == null){
      prof.send(Opcodes.POPUP_MSG, s"No request found for $id")
      return
    }
    val response: Byte = data getByte "response"
    request._type match{
      case Profile.Request.Friend =>
        response match{
          case Yes =>
            prof._friends += request._from
            request.from._friends += prof.login
            prof.requests remove id
            request.from.requests remove id
            prof.send(Opcodes.REQUEST_RESPONSE, request, response)
            prof.send(Opcodes.ADD_FRIEND, request.from)
            if(request.from.connected){
              request.from.send(Opcodes.REQUEST_RESPONSE, request, response)
              request.from.send(Opcodes.ADD_FRIEND, prof)
            }
            ProfileManager.save()
          case No | Cancel =>
            prof.requests remove id
            request.from.requests remove id
            prof.send(Opcodes.REQUEST_RESPONSE, request, response)
            if(request.from.connected)
              request.from.send(Opcodes.REQUEST_RESPONSE, request, response)
            ProfileManager.save()
          case _ => con.send(Opcodes.POPUP_MSG, s"invalid response ($response) for request $id")
        }
    }
  }

}