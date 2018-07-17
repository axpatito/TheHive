package controllers

import javax.inject.{ Inject, Singleton }
import org.elastic4play.Timed
import org.elastic4play.controllers.{ Authenticated, Fields, FieldsBodyParser, Renderer }
import org.elastic4play.services.{ AuxSrv, QueryDSL }
import play.api.http.Status
import play.api.mvc._
import models.Roles
import services.CaseReportSrv

import scala.concurrent.ExecutionContext

@Singleton
class CaseReportCtrl @Inject() (
    auxSrv: AuxSrv,
    authenticated: Authenticated,
    renderer: Renderer,
    components: ControllerComponents,
    fieldsBodyParser: FieldsBodyParser,
    caseReportSrv: CaseReportSrv,
    implicit val ec: ExecutionContext) extends AbstractController(components) with Status {

  def create: Action[AnyContent] = Action { implicit request ⇒
    Ok
  }

  @Timed
  def update(caseId: String): Action[Fields] = authenticated(Roles.admin).async(fieldsBodyParser) { implicit request ⇒
    caseReportSrv.update(caseId, request.body).map(template ⇒ renderer.toOutput(OK, template.toJson))
  }

  @Timed
  def find: Action[Fields] = authenticated(Roles.read).async(fieldsBodyParser) { implicit request ⇒
    val query = QueryDSL.any
    val range = request.body.getString("range")
    val sort = request.body.getStrings("sort").getOrElse(Nil)

    val (reports, total) = caseReportSrv.find(query, range, sort)
    renderer.toOutput(OK, reports.map(_.toJson), total)
  }
}