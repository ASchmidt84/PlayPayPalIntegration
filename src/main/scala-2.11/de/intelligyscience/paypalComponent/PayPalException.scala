package de.intelligyscience.paypalComponent

/**
  * Created by andre on 08.01.16.
  */
sealed class PayPalException(msg: String, cause: Throwable) extends RuntimeException(msg,cause)