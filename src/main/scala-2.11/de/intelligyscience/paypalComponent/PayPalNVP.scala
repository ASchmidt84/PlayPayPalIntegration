package de.intelligyscience.paypalComponent

import scala.util.{Failure, Success, Try}
import scalaj.http.Http

/**
  * Created by andre on 08.01.16.
  */
case class PayPalNVP(private val paypalUser: String,
                     private val paypalPass: String,
                     private val paypalSignature: String,
                     private val paypalVersion: String = "124.0",
                     private val testMode: Boolean = false) {

  private val url = if(testMode) "https://api-3t.sandbox.paypal.com/nvp" else "https://api-3t.paypal.com/nvp"

  private val paramList = (method: String) => List(
    "METHOD" -> method,
    "VERSION" -> paypalVersion,
    "USER" -> paypalUser,
    "PWD" -> paypalPass,
    "SIGNATURE" -> paypalSignature
  )

  /**
    * Initiate the payment sequence. The first step of payment
    * @param parameter A seq of PayPal parameters e.g. "PAYMENTREQUEST_0_PAYMENTACTION" -> "Sale"
    * @return Returns the URL to redirect the User to
    */
  def setExpressCheckout(parameter: Seq[(String,String)]) = {
    assert( parameter.exists(_._1 == "PAYMENTREQUEST_0_AMT"), "The PAYMENTREQUEST_0_AMT must be set!" )
    assert( parameter.exists(_._1 == "PAYMENTREQUEST_0_CURRENCYCODE"), "The PAYMENTREQUEST_0_CURRENCYCODE must be set!" )
    assert( parameter.exists(_._1 == "PAYMENTREQUEST_0_PAYMENTACTION"), "The PAYMENTREQUEST_0_PAYMENTACTION must be set!" )
    assert( parameter.exists(_._1 == "RETURNURL"), "The RETURNURL must be set!" )
    assert( parameter.exists(_._1 == "CANCELURL"), "The CANCELURL must be set!" )

    Try(
      Http(url).params( paramList("SetExpressCheckout") ::: parameter.toList ).method("GET").timeout(60000,60000).asParams.body
    ) match {
      case Success(seq) if seq.exists(_._1.toLowerCase == "token") =>
        val token = seq.find(_._1.toLowerCase == "token").get
        seq.find(_._1.toLowerCase == "ack") match {
          case Some((_,state)) if state.toLowerCase == "success" =>
            s"https://paypal.com/cgi-bin/webscr?cmd=_express-checkout&token=$token"
          case _ => throw new PayPalAckException()
        }
      case Failure(err) => throw new PayPalException("Error in paypal request",err)
    }
  }


  /**
    *
    * @param token The token gotten from setExpressCheckout
    * @param parameter A parameter Sequence for GetExpressCheckoutDetails (paypal API NVP)
    * @return The answer parameters of the paypal GetExpressCheckoutDetails request
    */
  def getExpressCheckoutDetails(token: String, parameter: Seq[(String,String)]) = {
    //val params = ("TOKEN" -> token) :: paramList("GetExpressCheckoutDetails") ::: parameter.toList
    executeHttp( url, ("TOKEN" -> token) :: paramList("GetExpressCheckoutDetails") ::: parameter.toList )
    /*Try( Http(url).timeout(6000000,6000000).postForm(params).asParams.body ) match {
      case Success(seq) if seq.exists(_._1.toLowerCase == "token") && token.toLowerCase == seq.find(_._1.toLowerCase == "token").get._2.toLowerCase =>
        seq.find(_._1.toLowerCase == "ack") match {
          case Some((_,ack)) if ack.toLowerCase == "success" =>
            seq
          case _ => throw new PayPalAckException()
        }
      case Failure(err) => throw new PayPalException("Error in paypal request",err)
    }*/
  }

  /**
    * Last step to get the money.
    * @param token The token
    * @param parameter Parameter sequence must contain PAYERID, PAYMENTREQUEST_0_AMT, PAYMENTREQUEST_0_PAYMENTACTION, PAYMENTREQUEST_0_CURRENCYCODE
    * @return
    */
  def doExpressCheckoutPayment(token: String, parameter: Seq[(String,String)]) = {
    assert( parameter.exists(_._1 == "PAYERID"), "The PAYERID must be set!" )
    assert( parameter.exists(_._1 == "PAYMENTREQUEST_0_AMT"), "The PAYMENTREQUEST_0_AMT must be set!" )
    assert( parameter.exists(_._1 == "PAYMENTREQUEST_0_PAYMENTACTION"), "The PAYMENTREQUEST_0_PAYMENTACTION must be set!" )
    assert( parameter.exists(_._1 == "PAYMENTREQUEST_0_CURRENCYCODE"), "The PAYMENTREQUEST_0_CURRENCYCODE must be set!" )
    //val params = List("TOKEN" -> token) ::: paramList("DoExpressCheckoutPayment") ::: parameter.toList
    executeHttp( url, List("TOKEN" -> token) ::: paramList("DoExpressCheckoutPayment") ::: parameter.toList )
  }

  private def executeHttp(url: String, formParams: Seq[(String,String)]) = Try( Http(url).timeout(6000000,6000000).postForm(formParams).asParams.body ) match {
    case Success(seq) if seq.exists(_._1.toLowerCase == "token") =>
      seq.find(_._1.toLowerCase == "ack") match {
        case Some((_,ack)) if ack.toLowerCase == "success" =>
          seq
        case _ => throw new PayPalAckException()
      }
    case Failure(err) => throw new PayPalException("Error in paypal request",err)
  }

  def getPayerId(seq: Seq[(String,String)]) = seq.find(_._1 == "PAYERID").map(_._2)

}
