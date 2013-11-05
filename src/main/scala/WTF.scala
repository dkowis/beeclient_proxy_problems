import co.freeside.betamax.{TapeMode, Recorder}
import co.freeside.betamax.proxy.jetty.ProxyServer
import java.io.{File, IOException, BufferedReader, InputStreamReader}
import java.net.{URLConnection, InetSocketAddress, URL, Proxy}
import javax.net.ssl.HttpsURLConnection
import uk.co.bigbeeconsultants.http.{Config, HttpClient}

object WTF extends App {



  println("Firing up the proxy")
  val recorder = new Recorder()
  recorder.setSslSupport(true)

  val proxyServer = new ProxyServer(recorder)

  //TAPES
  recorder.setTapeRoot(new File("tapes"))
  recorder.setDefaultMode(TapeMode.READ_WRITE)
  recorder.insertTape("testTape")
  proxyServer.start()

  //Do some http proxy stuff
  println("This should honor the -Dhttp.proxyHost and such settings")
  val client = new HttpClient()
  val url = new URL("https://www.example.com/test")
  val response = client.get(url)

  println(s"response code: ${response.status.code}")
  assert(response.status.code == 200)
  println("Body")
  println(response.body.toString)
  assert(response.body.toString == "Hey look some text", "Using Bee Client without a proxy configured")

  println("Making a request with the configured proxy")
  val proxyAddress = new InetSocketAddress("localhost", 5555)
  val conf = Config(
    sslSocketFactory = Some(SSLValidation.socketFactory),
    hostnameVerifier = Some(SSLValidation.hostnameVerifier),
    proxy = new Proxy(Proxy.Type.HTTP, proxyAddress)
  )

  val proxyClient = new HttpClient(conf)
  val proxyResponse = proxyClient.get(url)

  println(s"Response code: ${proxyResponse.status.code}")
  println("Body")
  println(proxyResponse.body.toString)

  assert(proxyResponse.status.code == 200, "Manual proxy specification")
  assert(proxyResponse.body.toString == "Hey look some text")


  //Trying with a HTTPSURLConnection directly
  println("Trying the HTTPS URL connection directly...")
  httpsUrl()

  println("Turning off the proxy")
  recorder.ejectTape()
  proxyServer.stop()

  def httpsUrl() = {
    val url = new URL("https://www.example.com/test")

    val con = url.openConnection().asInstanceOf[HttpsURLConnection]

    //Override the ssl validation stuff so that it's more dumb.
    con.setSSLSocketFactory(SSLValidation.socketFactory)
    con.setHostnameVerifier(SSLValidation.hostnameVerifier)

    printContent(con)
  }

  def printContent(con: URLConnection) = {
    if (con != null) {
      try {
        println("****** Content of the URL ********")
        val br =
          new BufferedReader(
            new InputStreamReader(con.getInputStream()))

        val str = Stream.continually(br.readLine()).takeWhile( _ != null).mkString("\n")
        println(str)

        br.close()

      } catch {
        case e: IOException =>
          e.printStackTrace()
      }

    }

  }

}

