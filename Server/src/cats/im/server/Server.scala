package cats.im.server

import cats.net.server.{ClientConnection, NonBlockingServer}
import cats.im.server.profile.{ProfileManager, Profile}
import cats.net.core.Core
import java.io.File
import cats.net.server.event.ServerListener

class Server extends NonBlockingServer(4595) with ServerListener[Server]{

  def init(){
    Core.verbose = true
    Core.verboseExceptions = true
    Core.addDataFormers(new File("./Server/xml/formers.xml"))
    addHandlers(new File("./Server/xml/handlers.xml"))
    Profile.registerCodec()
    ProfileManager.load()
    addListener(this)
  }

  def onLeave(server: Server, con: ClientConnection){
    val prof = ProfileManager get con
    if(prof == null)
      return
    con.attach(null)
    prof.con = null
    prof.setStatus(Profile.Status.Offline)
    prof.connectedFriends.foreach(_.send(Opcodes.CHANGE_STATUS, prof))
    ProfileManager.save()
  }

  def onJoin(server: Server, con: ClientConnection){}

}

object Server{

  val Login: String = "CIM"
  val Profile: Profile = new Profile(Login, null)

  def main(args: Array[String]) = new Server().start()
}
