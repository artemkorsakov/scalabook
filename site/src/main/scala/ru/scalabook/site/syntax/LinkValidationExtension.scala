package ru.scalabook.site.syntax

import laika.api.bundle.ExtensionBundle
import laika.ast.*
import laika.ast.RewriteRules.RewritePhaseBuilder
import sttp.client4.SttpClientException.ResponseHandlingException
import sttp.client4.quick.*

import scala.concurrent.duration.*
import scala.util.{Failure, Success, Try}

object LinkValidationExtension extends ExtensionBundle:

  val description: String = "Link Validation Extension for site's validation"

  private val correctCodes: Set[Int] = Set(302, 307, 429)
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
              println(s"Type of link is not recognized: ${link.getClass}")
              rewrite(url, SpanSequence(Text(url)))
            case _ => RewriteAction.Retain
  }

  override def rewriteRules: RewritePhaseBuilder =
    newRules.asDefaultPhaseBuilder

  private def rewrite(url: String, span: Span): RewriteAction[Span] =
    if blockedHosts.exists(url.startsWith) then RewriteAction.Replace(span)
    else if isStatusCodeValid(url) then RewriteAction.Retain
    else RewriteAction.Replace(span)

  private def isStatusCodeValid(url: String): Boolean =
    Try(quickRequest.get(uri"$url").readTimeout(5.seconds).send()) match
      case Success(response) =>
        if response.code.isSuccess then true
        else
          println(
            s"Found an invalid external link: $url, status code: ${response.code}"
          )
          false
      case Failure(error) =>
        error match
          case rhe: ResponseHandlingException[?] =>
            val response = rhe.responseException.response
            if correctCodes.contains(response.code.code) then true
            else
              println(
                s"Found an invalid external link: $url, the response was: ${rhe.responseException.response}"
              )
              false
          case _ =>
            println(
              s"Found an invalid external link: $url, the error was: $error"
            )
            false

end LinkValidationExtension
