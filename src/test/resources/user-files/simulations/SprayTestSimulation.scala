
import io.gatling.core.Predef._
import io.gatling.core.session.Expression
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.Headers.Names._
import io.gatling.http.Headers.Values._
import org.apache.commons.codec.digest.DigestUtils
import scala.concurrent.duration._
import bootstrap._
import assertions._

class SprayTestSimulation extends Simulation {

	val httpProtocol = http
		.baseURL("http://localhost:8080")
		.acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
		.acceptEncodingHeader("gzip, deflate, sdch")
		.acceptLanguageHeader("fr-FR,fr;q=0.8,en-US;q=0.6,en;q=0.4")
		.connection("keep-alive")
		.userAgentHeader("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/28.0.1500.71 Chrome/28.0.1500.71 Safari/537.36")

	val scn = scenario("Spray Test")
		.exec(http("verb_1")
			.get("/verb")
            .check(bodyString.is("GET")))
		.exec(http("verb_2")
			.post("/verb")
            .check(bodyString.is("POST")))
        .exec(http("hashQueryParameters_1")
            .get("/hashQueryParameters")
            .check(bodyString.is(DigestUtils.sha1Hex(""))))
        .exec(http("hashQueryParameters_2")
            .get("/hashQueryParameters?hello=world&str=!")
            .check(bodyString.is(DigestUtils.sha1Hex("world!"))))
        .exec(http("htmlIpsum_1")
            .get("/htmlIpsum")
            .check(headerRegex(CONTENT_TYPE, "text/html"))
            .check(header(CONTENT_LENGTH).is("1274")))
        .exec(http("soapXml_1")
            .get("/soapXml")
            .check(headerRegex(CONTENT_TYPE, "text/xml"))
            .check(header(CONTENT_LENGTH).is("1777")))
        .exec(http("jsonObject_1")
            .get("/jsonObject")
            .check(headerRegex(CONTENT_TYPE, "application/json"))
            .check(header(CONTENT_LENGTH).is("207")))
        .exec(http("jsonArray_1")
            .get("/jsonArray")
            .check(headerRegex(CONTENT_TYPE, "application/json"))
            .check(header(CONTENT_LENGTH).is("307")))
        .exec(http("cookies_1")
            .get("/cookies/countCookies")
            .check(regex("have 0 cookie").exists))
        .exec(http("cookies_2")
            .get("/cookies/take")
            .check(header(SET_COOKIE).exists))
        .exec(http("cookies_3")
            .get("/cookies/countCookies")
            .check(regex("have 1 cookie").exists)
            .check(header(SET_COOKIE).notExists))
        .exec(http("cookies_4")
            .get("/cookies/take")
            .check(header(SET_COOKIE).exists))
        .exec(http("cache_1")
            .get("/cache/expires")
            .check(header(EXPIRES).exists)
            .check(bodyString.saveAs("cacheExpires")))
        .exec(http("cache_2")
            .get("/cache/expires")
            .check(header(EXPIRES).exists)
            .check(bodyString.is("${cacheExpires}")))
        .exec(http("cache_3")
            .get("/cache/maxAge")
            .check(header(CACHE_CONTROL).is("max-age=300"))
            .check(bodyString.saveAs("cacheMaxAge")))
        /*
         * Ce test incrémente la valeur alors qu'il ne devrait pas ?!
        .exec(http("cache_4")
            .get("/cache/maxAge")
            .check(header(CACHE_CONTROL).is("max-age=300"))
            .check(bodyString.is("${cacheMaxAge}")))
         */
         .exec(http("admin_1")
             .get("/admin")
             .check(header(WWW_AUTHENTICATE).is("""Basic realm="Secured Resource""""))
             .check(status.is(401)))
         .exec(http("admin_2")
             .get("/admin")
             .check(header(WWW_AUTHENTICATE).not("""Basic realm="Secured Resource""""))
             .basicAuth("toto", "titi")
             .check(status.is(401)))

	setUp(scn.inject(atOnce(1 user))).protocols(httpProtocol)
}
