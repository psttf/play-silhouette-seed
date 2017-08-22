package data

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models.User
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

final class UserTable(tag: Tag) extends Table[User](tag, "User") {

//    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
//    def email = column[String]("email")
//    def isAdmin = column[Boolean]("isAdmin")
//    def passwordSaltAndHash = column[String]("passwordSaltAndHash")
//
//    override def * : ProvenShape[User] =
//      (id.?, email, isAdmin, passwordSaltAndHash) <>
//        ((User.apply _).tupled, User.unapply)

  def userID = column[UUID]("userID", O.PrimaryKey)
  def providerID = column[String]("providerID")
  def providerKey = column[String]("providerKey")
  def firstName = column[Option[String]]("firstName")
  def lastName = column[Option[String]]("lastName")
  def fullName = column[Option[String]]("fullName")
  def email = column[Option[String]]("email")
  def activated = column[Boolean]("activated")
  def hasher = column[String]("hasher")
  def password = column[String]("password")
  def salt = column[Option[String]]("salt")

  override def * : ProvenShape[User] =
    (
      userID,
      providerID,
      providerKey,
      firstName,
      lastName,
      fullName,
      email,
      activated,
      hasher,
      password,
      salt
    ) <>
      ((User.apply _).tupled, User.unapply)

}

object UserQueries {

  val table = TableQuery[UserTable]

  val findOne = Compiled( (providerID: Rep[String], providerKey: Rep[String]) =>
    table
      .filter(_.providerID === providerID)
      .filter(_.providerKey === providerKey)
  )

  val byUserID = Compiled( (id: Rep[UUID]) =>
    table.filter(_.userID === id)
  )

  val byEmail = Compiled( (email: Rep[String]) =>
    table.filter(_.email === email)
  )

//  val byId = Compiled( (id: Rep[Int]) =>
//    table.filter(_.id === id)
//  )

//  val otherAdminsCount = Compiled( (id: Rep[Int]) =>
//    table
//      .filter(_.id =!= id)
//      .filter(_.isAdmin === true)
//      .distinctOn(_.id)
//      .length
//  )

  val passwordInfoByLoginInfo = Compiled(
    (providerID: Rep[String], providerKey: Rep[String]) =>
      findOne.extract(providerID, providerKey)
        .map(user => (user.hasher, user.password, user.salt))
  )

  val schema = table.schema

}
