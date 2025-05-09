package ru.scalabook.site.syntax

import laika.api.bundle.ExtensionBundle
import laika.ast.*
import laika.ast.RewriteRules.RewritePhaseBuilder
import sttp.client4.Response
import sttp.client4.SttpClientException.{
  ReadException,
  ResponseHandlingException,
  TooManyRedirectsException
}
import sttp.client4.quick.*

import scala.concurrent.duration.*
import scala.util.{Failure, Success, Try}

object LinkValidationExtension extends ExtensionBundle:

  val description: String = "Link Validation Extension for site's validation"

  private val correctCodes: Set[Int] = Set(302, 307, 403, 429)
  private val blockedHosts: Set[String] = Set(
    "https://www.coursera.org",
    "https://rockthejvm.com",
    "https://blog.rockthejvm.com",
    "https://medium.com"
  )

  private val newRules: RewriteRules = RewriteRules.forSpans {
    case link: GlobalLink =>
      link match
        case image @ Image(ExternalTarget(url), _, _, _, _, _)
            if url.startsWith("http") =>
          rewrite(url, SpanSequence(Text(image.alt.getOrElse("No image"))))
        case _ @RawLink(ExternalTarget(url), _) if url.startsWith("http") =>
          rewrite(url, SpanSequence(Text(url)))
        case _ @SpanLink(content, ExternalTarget(url), _, _)
            if url.startsWith("http") =>
          rewrite(url, SpanSequence(content))
        case _ =>
          link.target match
            case ExternalTarget(url) if url.startsWith("http") =>
              println(
                s"[WARNING] type of link is not recognized: ${link.getClass}"
              )
              rewrite(url, SpanSequence(Text(url)))
            case _ => RewriteAction.Retain
  }

  override def rewriteRules: RewritePhaseBuilder =
    newRules.asDefaultPhaseBuilder

  private def rewrite(url: String, span: Span): RewriteAction[Span] =
    if blockedHosts.exists(url.startsWith) then
      println(s"[info] link to blocked resource: $url")
      RewriteAction.Replace(span)
    else if isStatusCodeValid(url) then RewriteAction.Retain
    else RewriteAction.Replace(span)

  private def isStatusCodeValid(url: String): Boolean =
    sendRequest(url) match
      case Success(response) => isResponseValid(url, response)
      case Failure(error)    => isErrorValid(url, error)

  private def sendRequest(url: String): Try[Response[Unit]] =
    Try {
      quickRequest.head(uri"$url").response(ignore).readTimeout(5.seconds).send()
    } match
      case Success(response) if response.code.code == 405 =>
        Try {
          quickRequest.get(uri"$url").response(ignore).readTimeout(5.seconds)
            .send()
        }
      case other => other

  private def isResponseValid(url: String, response: Response[Unit]): Boolean =
    if response.code.isSuccess then true
    else if correctCodes.contains(response.code.code) then
      println(
        s"[info] status code suspicious ${response.code.code}, please check url manually: $url"
      )
      true
    else
      println(
        s"[WARNING] found an invalid external link: $url, status code: ${response.code}"
      )
      false

  private def isErrorValid(url: String, error: Throwable): Boolean =
    error match
      case rhe: ResponseHandlingException[?] =>
        val response = rhe.responseException.response
        if correctCodes.contains(response.code.code) then
          println(
            s"[info] status code suspicious ${response.code.code}, please check url manually: $url"
          )
          true
        else
          println(
            s"[WARNING] found an invalid external link: $url, response: $response"
          )
          false
      case _: TooManyRedirectsException =>
        println(s"[info] too many redirects, please check url manually: $url")
        true
      case _: ReadException =>
        println(
          s"[info] something wrong with reading, please check url manually: $url"
        )
        true
      case _ =>
        println(
          s"[WARNING] found an invalid external link: $url, error: $error"
        )
        false

end LinkValidationExtension
