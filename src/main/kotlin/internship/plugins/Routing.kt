package internship.plugins

import com.google.gson.Gson
import internship.jsonsRoot
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.content.*
import io.ktor.http.content.*
import java.time.*
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class Field(
    val type: String?,
    val value: String?
)
data class RequestData(
    val userToken: String,
    val fieldsValue: ArrayList<Field?>?
)

fun Application.configureRouting() {
    routing {
        static("/"){
            resources("templates")
            resources("/jsons")
            resources("/")
            resource("/", "templates/index.html")
            resource("/admin", "templates/admin.html")
        }
        static("/static") {
            resources("static")
        }
        get("/webjars") {
            call.respondText("<script src='/webjars/jquery/jquery.js'></script>", ContentType.Text.Html)
        }

        route("/api/"){
            get("json"){

                withContext(Dispatchers.IO){
                    //var f = File("src/main/resources/jsons/widgets.json")
                    val loader = javaClass.classLoader.getResourceAsStream("jsons/widgets.json")
                    //if (!f.exists()) f = File("jsons/widgets.json")
                    val txt = loader.bufferedReader().use { it.readText() }
                    call.respondText(txt)
                }
            }

            get("jsonEx"){
                withContext(Dispatchers.IO){
                    println("root "+ jsonsRoot.exists())
                    jsonsRoot.listFiles()?.forEach {
                        println(it.absolutePath)
                    }
                    call.respondText("rrr")
                }
            }
            get("jsonExe"){
                withContext(Dispatchers.IO){
                    println("res as stre /" + javaClass.classLoader.getResourceAsStream("/jsons/widgets.json")
                        ?.bufferedReader().use { it?.readText() })
                    println("res as stre " + javaClass.classLoader.getResourceAsStream("jsons/widgets.json")
                        ?.bufferedReader().use { it?.readText() })
                    val r = javaClass.classLoader.getResourceAsStream("/jsons/widgets.json")
                        ?.bufferedReader().use { it?.readText() } ?: javaClass.classLoader.getResourceAsStream("jsons/widgets.json")
                        ?.bufferedReader().use { it?.readText() }
                    call.respondText(r.toString())
                }
            }

            post("123"){
                val json = call.receive<String>()
                print(json)
                val fields = Gson().fromJson(json, RequestData::class.java)

                call.respond(fields)
                //call.respondText(json)
                //call.respondText("post respond OK")
            }
            post("json"){
                withContext(Dispatchers.IO){
                    val data = call.receive<String>()
                    println(data)
                    val resUrl = javaClass.classLoader.getResource("jsons/widgets.json")
                    val out = FileOutputStream(File(resUrl.toURI()))
                    out.bufferedWriter().use {  out ->
                        out.write("{\"widgets\":" + data + "\n\n\n}") }
                    out.flush()
                   val f = javaClass.classLoader.getResourceAsStream("jsons/widgets.json")
                    call.respondText(f.bufferedReader().use { it.readText() })
                }
            }
        }
    }
}
