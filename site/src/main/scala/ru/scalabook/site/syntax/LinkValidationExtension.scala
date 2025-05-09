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

  private val newRules: RewriteRules = RewriteRules.forSpans {
    case link: GlobalLink =>
      link match
        case image @ Image(ExternalTarget(url), _, _, _, _, _)
            if url.startsWith("http") =>
          if isStatusCodeValid(url) then RewriteAction.Retain
          else
            RewriteAction.Replace(
              SpanSequence(Text(image.alt.getOrElse("No image")))
            )
        case _ @RawLink(ExternalTarget(url), _)
            if url.startsWith("http") =>
          if isStatusCodeValid(url) then RewriteAction.Retain
          else RewriteAction.Replace(SpanSequence(Text(url)))
        case spanLink @ SpanLink(ExternalTarget(url), _, _, _)
            if url.startsWith("http") =>
          if isStatusCodeValid(url) then RewriteAction.Retain
          else RewriteAction.Replace(SpanSequence(spanLink.content))
        case _ =>
          println(s"Type of link is not recognized: ${link.getClass}")
          link.target match
            case ExternalTarget(url) if url.startsWith("http") =>
              if isStatusCodeValid(url) then RewriteAction.Retain
              else RewriteAction.Replace(SpanSequence(Text(url)))
            case _ => RewriteAction.Retain
  }

  override def rewriteRules: RewritePhaseBuilder =
    newRules.asDefaultPhaseBuilder

  private def isStatusCodeValid(url: String): Boolean =
    Try(quickRequest.get(uri"$url").readTimeout(5.seconds).send()) match
      case Failure(error) =>
        error match
          case rhe: ResponseHandlingException[?] =>
            println(
              s"Found an invalid external link: $url, the error was: ${rhe.responseException.response}"
            )
          case _ =>
            println(
              s"Found an invalid external link: $url, the error was: $error"
            )
        false
      case Success(response) =>
        if response.code.isSuccess then true
        else
          println(
            s"Found an invalid external link: $url, status code: ${response.code}"
          )
          false

end LinkValidationExtension
