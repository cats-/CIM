package cats.im.server.profile

import java.io.{FileOutputStream, DataOutputStream, FileInputStream, DataInputStream, File}
import cats.net.core.buffer.{BufferBuilder, Buffer}
import cats.net.server.ClientConnection

object ProfileManager {

  private val File = new File("/res/saved/profiles.dat")
  
  private var all: String Map Profile = Map()
  
  def get(login: String): Profile = {
    if(login == null)
      return null
    all.get(login.toLowerCase).getOrElse(null)
  }

  def contains(login: String): Boolean = get(login) != null

  def get(prof: Profile): Profile = {
    if(prof == null)
      return null
    get(prof.login)
  }

  def get(con: ClientConnection): Profile = {
    if(con.attachment == null)
      return null
    get(con.attachment[Profile].login)
  }

  def ++(login: String, pass: String): Profile = {
    val prof = new Profile(login, pass)
    all += (prof.login.toLowerCase -> prof)
    prof
  }

  def load(){
    if(!File.exists)
      return
    val in = new DataInputStream(new FileInputStream(File))
    if(in.available <= 0){
      in.close()
      return
    }
    val bytes: Array[Byte] = new Array(in.readInt)
    in.readFully(bytes)
    val buffer = Buffer.wrap(bytes)
    val count = buffer.getInt
    for(i <- 0 until count){
      val prof = buffer.getObject[Profile]
      all += (prof.login.toLowerCase -> prof)
    }
    in.close()
  }

  def save(){
    val bldr = new BufferBuilder
    bldr putInt all.size
    all.values.foreach(bldr putObject)
    val buf = bldr.create
    val out = new DataOutputStream(new FileOutputStream(File))
    out writeInt buf.size
    out write buf.array
    out.flush()
    out.close()
  }

}
