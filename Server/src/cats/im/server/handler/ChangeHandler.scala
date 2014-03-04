package cats.im.server.handler

import cats.im.server.{Opcodes, Server}
import cats.net.server.handler.ServerDataHandler
import cats.net.core.data.Data
import cats.net.server.ClientConnection
import cats.im.server.profile.{Profile, ProfileManager}
import cats.im.server.utils.Utils

class ChangeHandler extends ServerDataHandler[Server]{

  def getOpcodes = Array(Opcodes.CHANGE_MOOD, Opcodes.CHANGE_NAME, Opcodes.CHANGE_STATUS, Opcodes.CHANGE_PIC, Opcodes.CHANGE_ACCESS)

  def handle(server: Server, con: ClientConnection, data: Data){
    val prof: Profile = ProfileManager get con
    var madeChange: Boolean = false
    data.opcode match{
      case Opcodes.CHANGE_MOOD =>
        val mood: String = data getString "arg"
        if(mood != null){
          prof.mood = mood
          madeChange = true
        }
      case Opcodes.CHANGE_NAME =>
        val name: String = data getString "arg"
        if(!Utils.isEmpty(name)){
          prof.name = name
          madeChange = true
        }
      case Opcodes.CHANGE_STATUS =>
        val status: Byte = data getByte "arg"
        if(Profile.Status.isOneOf(status)){
          prof.setStatus(status)
          madeChange = true
        }
      case Opcodes.CHANGE_PIC =>
        val picUrl: String = data getString "arg"
        if(!Utils.isEmpty(picUrl)){
          prof.picUrl = picUrl
          madeChange = true
        }
      case Opcodes.CHANGE_ACCESS =>
        val access: Byte = data getByte "arg"
        if(Profile.Access.isOneOf(access)){
          prof.access = access
          madeChange = true
        }
    }
    if(!madeChange)
      return
    prof send data
    prof.connectedFriends.foreach(_ send data)
    ProfileManager.save()
  }
}
