package cats.im.server.profile

import cats.net.core.codec.{Decoder, Encoder}
import cats.net.core.buffer.{Buffer, BufferBuilder}
import scala.collection.mutable.ListBuffer
import cats.net.core.Core
import cats.net.core.data.Data
import cats.net.server.ClientConnection
import cats.net.core.data.former.DataFormer
import cats.im.server.Opcodes
import scala.collection.mutable

class Profile(val login: String, val pass: String) {

  var name: String = login
  var access: Byte = Profile.Access.None
  var status: Byte = Profile.Status.Online
  var lastStatus: Byte = status
  var mood: String = ""
  var picUrl: String = ""
  var _friends: ListBuffer[String] = ListBuffer()
  var requests: mutable.Map[Long, Profile.Request] = mutable.Map()
  private val offlineQueue: mutable.Queue[Data] = mutable.Queue()
  var con: ClientConnection = null

  def connected: Boolean = con != null && con.isConnected

  def setStatus(status: Byte){
    lastStatus = this.status
    this.status = status
  }

  def friends: ListBuffer[Profile] = {
    for(f <- _friends)
      yield ProfileManager get f
  }

  def connectedFriends: ListBuffer[Profile] = {
    for(f <- friends if f.connected)
      yield f
  }

  def addToOfflineQueue(data: Data){
    if(data.opcode == Opcodes.MSG)
      offlineQueue += data
  }

  def addRequest(request: Profile.Request){
    requests += (request.id -> request)
  }

  def getRequest(id: Long): Profile.Request = {
    val opt = requests get id
    if(opt.isEmpty || !opt.isDefined)
      null
    else
      opt.get
  }

  def sendOfflineQueue(){
    while(connected && !offlineQueue.isEmpty)
      send(offlineQueue.dequeue())
  }

  def send(opcode: Short, args: Object*): Boolean = {
    if(!connected){
      val data = Core.getDataFormer[DataFormer](opcode).form(opcode, args.toArray)
      addToOfflineQueue(data)
      return false
    }
    con.send(opcode, args.toArray)
  }

  def send(data: Data): Boolean = {
    if(!connected){
      addToOfflineQueue(data)
      return false
    }
    con.send(data)
  }

  override def equals(o: Any): Boolean = {
    o match{
      case p: Profile => login == p.login
      case _ => false
    }
  }

}

object Profile{

  class Request(val _type: Byte, val _from: String, val _to: String, val id: Long = System.nanoTime) {

    def from: Profile = ProfileManager get _from
    def to: Profile = ProfileManager get _to

    override def equals(o: Any): Boolean = {
      o match{
        case req: Profile.Request => id == req.id
        case _ => false
      }
    }

  }

  object Request{

    val Friend: Byte = 0

    private val Encoder = new Encoder[Request]{
      def encode(bldr: BufferBuilder, req: Request){
        bldr putByte req._type
        bldr putString req._from
        bldr putString req._to
        bldr putLong req.id
      }
    }

    private val Decoder = new Decoder[Request]{
      def decode(buf: Buffer): Request = {
        new Request(buf.getByte, buf.getString, buf.getString, buf.getLong)
      }
    }

    def isOneOf(_type: Byte): Boolean = _type == Friend

    def registerCodec() = Core.addCodec(classOf[Request], Encoder, Decoder)

  }

  object Status{
    val Online: Byte = 0
    val Busy: Byte = 1
    val Away: Byte = 2
    val Offline: Byte = 3

    def isOneOf(status: Byte): Boolean = status >= Online && status <= Offline
  }
  
  object Access{
    val None: Byte = 0
    val Mod: Byte = 1
    val Admin: Byte = 2
    val Owner: Byte = 3

    def isOneOf(access: Byte): Boolean = access >= None && access <= Owner
  }

  private val Encoder = new Encoder[Profile]{
    def encode(bldr: BufferBuilder, prof: Profile){
      bldr putString prof.login
      bldr putString prof.pass
      bldr putString prof.name
      bldr putByte prof.access
      bldr putByte prof.lastStatus
      bldr putByte prof.status
      bldr putString prof.mood
      bldr putString prof.picUrl
      bldr putInt prof._friends.size
      prof._friends.foreach(bldr.putString)
      bldr putInt prof.requests.size
      prof.requests.values.foreach(bldr putObject)
      bldr putInt prof.offlineQueue.size
      prof.offlineQueue.foreach(bldr putObject)
    }
  }

  private val Decoder = new Decoder[Profile]{
    def decode(buf: Buffer): Profile = {
      val prof = new Profile(buf.getString, buf.getString)
      prof.name = buf.getString
      prof.access = buf.getByte
      prof.lastStatus = buf.getByte
      prof.status = buf.getByte
      prof.mood = buf.getString
      prof.picUrl = buf.getString
      for(i <- 0 until buf.getInt)
        prof._friends += buf.getString
      for(i <- 0 until buf.getInt)
        prof addRequest buf.getObject[Profile.Request]
      for(i <- 0 until buf.getInt)
        prof.offlineQueue += buf.getObject[Data]
      prof
    }
  }

  def registerCodec() = {
    Core.addCodec(classOf[Profile], Encoder, Decoder)
    Profile.Request.registerCodec()
  }

}
