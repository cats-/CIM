package cats.im.server.handler

import cats.net.server.handler.ServerDataHandler
import cats.net.core.data.Data
import cats.net.server.ClientConnection
import cats.im.server.{Opcodes, Server}
import cats.im.server.profile.ProfileManager

class RegisterHandler extends ServerDataHandler[Server]{
  
  def getOpcodes = Array(Opcodes.REGISTER)
  
  def handle(server: Server, con: ClientConnection, data: Data){
    val login: String = data.getString("login").trim
    val pass: String = data.getString("pass").trim
    if(!validateLogin(con, login) || !validatePass(con, pass))
      return
    ProfileManager ++ (login, pass)
    con.send(Opcodes.POPUP_MSG, s"$login successfully registered")
  }

  private def validateCommon(con: ClientConnection, prefix: String, str: String): Boolean = {
    if(str contains " "){
      con.send(Opcodes.POPUP_MSG, s"$prefix cannot contain any spaces")
      return false
    }
    if(str.length < 3 || str.length > 20){
      con.send(Opcodes.POPUP_MSG, s"$prefix must be inbetween 3 and 20 characters")
      return false
    }
    true
  }

  private def validateLogin(con: ClientConnection, login: String): Boolean = {
    if(!validateCommon(con, "login", login))
      return false
    if(ProfileManager contains login){
      con.send(Opcodes.POPUP_MSG, s"$login is already taken")
      return false
    }
    true
  }

  private def validatePass(con: ClientConnection, pass: String): Boolean = {
    if(!validateCommon(con, "pass", pass))
      return false
    if(count(pass, Character.isDigit) < 1){
      con.send(Opcodes.POPUP_MSG, "pass must contain at least 1 digit")
      return false
    }
    if(count(pass, Character.isUpperCase) < 1){
      con.send(Opcodes.POPUP_MSG, "pass must contain at least 1 upper case character")
      return false
    }
    if(count(pass, Character.isLowerCase) < 1){
      con.send(Opcodes.POPUP_MSG, "pass must contain at least 1 lower case character")
      return false
    }
    if(count(pass, !_.isLetterOrDigit) < 1){
      con.send(Opcodes.POPUP_MSG, "pass must contain at least 1 symbol")
      return false
    }
    true
  }

  private def count(str: String, func: (Char) => Boolean): Int = {
    var count = 0
    for(c <- str.toCharArray if func(c))
      count += 1
    count
  }

}
