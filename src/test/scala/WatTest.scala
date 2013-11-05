import co.freeside.betamax.proxy.jetty.ProxyServer
import co.freeside.betamax.{TapeMode, Recorder}
import java.io.{File, IOException, InputStreamReader, BufferedReader}
import java.net.{URLConnection, Proxy, InetSocketAddress, URL}
import javax.net.ssl.HttpsURLConnection
import org.scalatest.{Matchers, BeforeAndAfterAll, FunSpec}
import uk.co.bigbeeconsultants.http.{Config, HttpClient}

class WatTest extends FunSpec with Matchers with BeforeAndAfterAll {

  var recorder: Recorder = null
  var proxyServer: ProxyServer = null

  override def beforeAll() = {
    println("Firing up proxy")
    recorder = new Recorder()
    recorder.setSslSupport(true)

    recorder.setTapeRoot(new File("tapes"))
    recorder.setDefaultMode(TapeMode.READ_WRITE)
    recorder.insertTape("testTape")

    proxyServer = new ProxyServer(recorder)
    proxyServer.start()
  }

  override def afterAll() = {
    println("Turning off the proxy")
    recorder.ejectTape()
    proxyServer.stop()
  }

  describe("All of the following should honor the java http Proxy settings") {
    it("Works with a Bee HttpClient normally") {
      val conf = Config(
        sslSocketFactory = Some(SSLValidation.socketFactory),
        hostnameVerifier = Some(SSLValidation.hostnameVerifier)
      )
      val client = new HttpClient(conf)
      val url = new URL("https://www.example.com/test")
      val response = client.get(url)

      response.status.code should equal(200)
      response.body.toString should equal("Hey look some text")
    }

    it("Works with a Bee HttpClient specifying the proxy by hand") {
      val proxyAddress = new InetSocketAddress("localhost", 5555)
      val conf = Config(
        sslSocketFactory = Some(SSLValidation.socketFactory),
        hostnameVerifier = Some(SSLValidation.hostnameVerifier),
        proxy = new Proxy(Proxy.Type.HTTP, proxyAddress)
      )
      val client = new HttpClient(conf)
      val url = new URL("https://www.example.com/test")
      val response = client.get(url)

      response.status.code should equal(200)
      response.body.asString should equal("Hey look some text")

    }

    it("Works with a Java HTTPUrlConnection") {
      def getContent(con: URLConnection): String = {
        if (con != null) {
          val br = new BufferedReader(new InputStreamReader(con.getInputStream))
          val str = Stream.continually(br.readLine()).takeWhile(_ != null).mkString("\n")
          br.close()
          str
        } else {
          null
        }
      }

      val url = new URL("https://www.example.com/test")

      val con = url.openConnection().asInstanceOf[HttpsURLConnection]

      //Override the ssl validation stuff so that it's more dumb.
      con.setSSLSocketFactory(SSLValidation.socketFactory)
      con.setHostnameVerifier(SSLValidation.hostnameVerifier)

      getContent(con) should equal("Hey look some text")

    }
  }

}
