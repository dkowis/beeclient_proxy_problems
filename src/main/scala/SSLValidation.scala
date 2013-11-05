import javax.net.ssl._
import java.security.cert.X509Certificate


object SSLValidation {

  class NaiveTrustManager extends X509TrustManager {
    def checkServerTrusted(p1: Array[X509Certificate], p2: String) = {
      //Nothing!
    }

    def checkClientTrusted(p1: Array[X509Certificate], p2: String) = {
      //Don't care!
    }

    def getAcceptedIssuers: Array[X509Certificate] = {
      //Empty array!
      Array[X509Certificate]()
    }
  }

  val socketFactory = {
    try {
      //Ssl context!
      val sc = SSLContext.getInstance("SSL")

      //Create a trust manager that doesn't validate anything!
      val trustAllCerts = new NaiveTrustManager

      sc.init(null, Array(trustAllCerts), new java.security.SecureRandom)

      sc.getSocketFactory()
    }
  }

  //TODO: could probably validate the hostname for real, via the config
  val hostnameVerifier = new HostnameVerifier {
    def verify(p1: String, p2: SSLSession): Boolean = true
  }

}
