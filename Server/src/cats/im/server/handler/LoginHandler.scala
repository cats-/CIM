package cats.im.server.handler

import cats.net.core.data.Data
import cats.net.server.ClientConnection
import cats.im.server.{Opcodes, Server}
import cats.net.server.handler.ServerDataHandler
import cats.im.server.profile.{Profile, ProfileManager}

class LoginHandler extends ServerDataHandler[Server]{

  def getOpcodes = Array(Opcodes.LOGIN)

  def handle(server: Server, con: ClientConnection, data: Data){
    val login: String = data.getString("login").trim
    val pass: String = data.getString("pass").trim
    val prof: Profile = ProfileManager get login
    if(prof == null){
      con.send(Opcodes.POPUP_MSG, s"$login does not exist")
      return
    }
    if(prof.connected){
      con.send(Opcodes.POPUP_MSG, s"$login is already logged in")
      return
    }
    if(!prof.pass.equals(pass)){
      con.send(Opcodes.POPUP_MSG, s"password mismatch for $login")
      return
    }
    con.attach(prof)
    prof.con = con
    prof.status = prof.lastStatus
    prof.send(Opcodes.INIT, prof)
    prof.requests.values.foreach(prof.send(Opcodes.REQUEST, _))
    prof.connectedFriends.foreach(
      f => {
        prof.send(Opcodes.ADD_FRIEND, f)
        f.send(Opcodes.ADD_FRIEND, prof)
      }
    )
    prof.sendOfflineQueue()
  }

}
