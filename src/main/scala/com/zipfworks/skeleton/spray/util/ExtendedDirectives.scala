package com.zipfworks.skeleton.spray.util

import com.zipfworks.skeleton.spray.Controller
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import spray.http.ContentTypes
import spray.httpx.SprayJsonSupport
import spray.httpx.marshalling.Marshaller
import spray.json._
import spray.routing.{Route, PathMatcher, PathMatcher1, Directives}

import scala.util.{Failure, Success}

trait ExtendedDirectives extends Directives with SprayJsonSupport with DefaultJsonProtocol {

  //Path matcher for getting a slug or ID
  val IdOrSlug: PathMatcher1[String] = PathMatcher("[a-z0-9A-Z_\\-]+".r)

  type FMapRes = Future[Map[String, JsValue]]
  def completeMap(f: FMapRes)(implicit ec: ExecutionContext): Route = onComplete(f){
    case Success(m) => complete(m)
    case Failure(e) => ERR(e).complete
  }

  /**********************************************************************************
    * Print Out Compact/Pretty depending on Production/Development
    *********************************************************************************/
  val printer = if(Controller.IS_PRODUCTION) CompactPrinter else PrettyPrinter

  override implicit def sprayJsonMarshallerConverter[T](writer: RootJsonWriter[T])(implicit printer: JsonPrinter = printer) =
    sprayJsonMarshaller[T](writer, printer)
  override implicit def sprayJsonMarshaller[T](implicit writer: RootJsonWriter[T], printer: JsonPrinter = printer) =
    Marshaller.delegate[T, String](ContentTypes.`application/json`) { value ⇒
      val json = writer.write(value)
      printer(json)
    }
}
