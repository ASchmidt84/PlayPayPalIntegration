package de.intelligyscience.paypalComponent

/**
  * Created by andre on 08.01.16.
  */
sealed class PayPalAckException extends RuntimeException("The ACK state is not success")
