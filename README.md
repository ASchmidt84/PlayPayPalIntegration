# PlayPayPalIntegration

This module may help to integrate paypal payment to the Play Framework. This module comes without any dependencies to Play. 

At the moment their is only NVP support with username, password and signature.

This is under MIT Licence

**How to user**

1. Create an instance of PayPalNVP
2. Initiate the payment sequence
3. Relink the user to the returned URL
4. Receiving the answer over the **RETURNURL** or **CANCELURL**
5. Execute *getExpressCheckoutDetails* to get the final parameters by PayPal
6. Get the money



**1. Create an instance of PayPalNVP**

```scala

    val paypal = PayPalNVP("user","password","Signature","124.0",false)

```

**2. Initiate the payment sequence**

```scala

    val parameter = Seq(
        "PAYMENTREQUEST_0_AMT"              -> "100.0",
        "PAYMENTREQUEST_0_CURRENCYCODE"     -> "EUR",
        "PAYMENTREQUEST_0_PAYMENTACTION"    -> "Sale",
        "RETURNURL"                         -> "http://anywhere.de/return/token",
        "CANCELURL"                         -> "http://anywhere.de/cancel/token"
    )
    val returnURL = paypal.setExpressCheckout(parameter)

```
You can set more parameters. These are the necessaries! For more parameter look at paypal api for nvp.

**3. Relink the user to the returned URL**
Please relink your customer through button action oder directly redirection to paypal. Your customer has to loggin or to
fill out all information and to cancel or submit the payment. The customer will be redirected to your site after he made
all necessary steps or cancel.

**4. Receiving the answer over the RETURNURL or CANCELURL** && **Execute *getExpressCheckoutDetails* to get the final parameters by PayPal**
Paypal redirects your customer to your website. It is necessary to accept this calls. The call method will be GET.

```
    GET         /paypal/success                            controllers.Paypal.displayReturn()
    GET         /paypal/cancel                             controllers.Paypal.displayCancel()
```

You will receiving the answer of paypal as GET Parameters.

```
    def displayReturn() = StackAction(AuthorityKey -> NormalUser){implicit req =>
        req.getQueryString("token") match {
          case Some(token) =>
            paypal.getExpressCheckoutDetails(token,Seq())
          case _ => Redirect(routes.UserDashboard.dashboard()).flashing("error" -> "MoneyTransaction has errors")
        }
    }

    def displayCancel() = StackAction(AuthorityKey -> NormalUser){implicit req =>
        Redirect(routes.UserDashboard.dashboard()).flashing("error" -> "MoneyTransaction was abort")
    }
```

You can add `paypal.getExpressCheckoutDetails(token,Seq())` an Sequence of `"" -> ""` parameters. NOTICE you have to look 
at the API of Paypal for *GetExpressCheckoutDetails*.
The method `paypal.getExpressCheckoutDetails(token,Seq())` post the form with all data to paypal. Sometimes developer used an
extra form with another submit button. Here we don't. This method returns the answer parameter of PayPal. You will receive 
an PayPalException if the transaction was not successfully!! Otherwise you get the Sequence!

**6. Get the money**

Yeah now it is time to get the money to your account :laughing:
The method `paypal.getExpressCheckoutDetails(token,Seq())` will return a `Seq[(String,String)]` and contains the answer parameter.
Attention you need the payerId. We offer you a method to extract them of the answer parameter sequence.
To use this method `paypal.getPayerId(parameterSeq)` Option of String is the result. Also necessary are  **"PAYMENTREQUEST_0_AMT",
"PAYMENTREQUEST_0_CURRENCYCODE","PAYMENTREQUEST_0_PAYMENTACTION"**!!!

```
    ...
    val result = paypal.getExpressCheckoutDetails(token,Seq())
    val parameter = Seq(
        "PAYMENTREQUEST_0_AMT"              -> "100.0",
        "PAYMENTREQUEST_0_CURRENCYCODE"     -> "EUR",
        "PAYMENTREQUEST_0_PAYMENTACTION"    -> "Sale",
        "PAYERID"                           -> paypal.getPayerId(result).getOrElse("")
    )
    paypal.doExpressCheckoutPayment(token,parameter)
```

If now you got an Exception you did something wrong. Read paypal api for this or create an issue. Otherwise you gotten 100 Euro. Congratulations.