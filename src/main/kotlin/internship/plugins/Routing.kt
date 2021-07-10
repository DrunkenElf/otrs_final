package internship.plugins

import com.google.gson.Gson
import internship.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter


val jsonsRootFile = File("resources/jsons").also {
    if (!it.exists()) it.mkdirs()
    val widgets = File(it, "widgets.json").also { child ->
        child.createNewFile()
        val writer = child.bufferedWriter()
        val json = Application::class.java.classLoader.getResourceAsStream("jsons/widgets.json")
            ?.bufferedReader().use { res -> res?.readText() }
        writer.write("" + json)
        writer.flush()
        writer.close()
    }
    val widgetsRU = File(it, "widgetsRU.json").also { child ->
        child.createNewFile()
        val writer = child.bufferedWriter()
        val json = Application::class.java.classLoader.getResourceAsStream("jsons/widgetsRU.json")
            ?.bufferedReader().use { res -> res?.readText() }
        writer.write("" + json)
        writer.flush()
        writer.close()
    }
    val list = File(it, "list.json").also { child ->
        child.createNewFile()
        val writer = child.bufferedWriter()
        val json = Application::class.java.classLoader.getResourceAsStream("jsons/list.json")
            ?.bufferedReader().use { res -> res?.readText() }
        writer.write(json)
        writer.flush()
        writer.close()
    }
    val listRU = File(it, "list-ru.json").also { child ->
        child.createNewFile()
        val writer = child.bufferedWriter()
        val json = Application::class.java.classLoader.getResourceAsStream("jsons/list-ru.json")
            ?.bufferedReader().use { res -> res?.readText() }
        writer.write(json)
        writer.flush()
        writer.close()
    }
}

fun Application.configureRouting() {
    routing {
        static("/") {
            resources("templates")
            resources("/")
            resource("/", "templates/index.html")
            resource("/admin", "templates/admin.html")
            resource("/login", "templates/login.html")
        }
        static("/static") {
            resources("static")
        }

        route("/api/") {
            get("json") {
                val txt = File("resources/jsons/widgets.json").readText()
                val userSession = call.sessions.get<UserSession>()
                val user = "{\n" +
                        "  \"session\": {\n" +
                        "    \"customerUser\": \"${userSession?.customerUser ?: ""}\",\n" +
                        "    \"sessionId\": \"${userSession?.sessionId ?: ""}\"\n" +
                        "  }," +
                        "  ${txt.replaceFirst("{", "")}"
                call.respond(user)
            }

            get("json_ru") {
                withContext(Dispatchers.IO) {
                    val txt = File("resources/jsons/widgetsRU.json").readText()
                    val userSession = call.sessions.get<UserSession>()
                    val user = "{\n" +
                            "  \"session\": {\n" +
                            "    \"customerUser\": \"${userSession?.customerUser ?: ""}\",\n" +
                            "    \"sessionId\": \"${userSession?.sessionId ?: ""}\"\n" +
                            "  }," +
                            "  ${txt.replaceFirst("{", "")}"
                    call.respond(user)
                }
            }

            get("user") {
                val userSession = call.sessions.get<UserSession>()
                println("usersesion: ${userSession?.sessionId} ${userSession?.customerUser}")
                val ids = getTicketIds(userSession!!)
                val tickets = getTicketsByIds(userSession, ids)
                call.respond(tickets)
            }

            post("createTicket") {
                val json = call.receive<String>()
                val requestData = Gson().fromJson(json, RequestData::class.java)
                println("fields values " + requestData.fieldsValue.toString())
                val userSession = call.sessions.get<UserSession>()
                val ticketResp = formTicketCreate(
                    Ticket(
                        title = requestData.fieldsValue?.find { it?.type.equals("Topic") || it?.type.equals("Тема") }?.value
                            ?: "npe",
                        customerUser = userSession!!.customerUser,
                        article = Article(
                            subject = requestData.widgetName,
                            body = requestData.fieldsValue?.find { it?.type.equals("Issue") || it?.type.equals("Проблема") }?.value + "\n" +
                                    requestData.fieldsValue?.filter {
                                        !it?.type.equals("Topic") && !it?.type.equals("Issue") &&
                                                !it?.type.equals("Тема") && !it?.type.equals("Проблема")
                                    }?.map { "${it?.type}: ${it?.value}\n" } +
                                    requestData.faq_addon,
                        ),
                    ),
                    userSession.sessionId
                )
                call.respond(ticketResp)
            }

            post("createSession") {
                val json = call.receive<String>()
                println("create ses: $json")
                val user = Gson().fromJson(json, User::class.java)
                val createSession = sessionCreate(user.user, user.password)
                if (createSession.isOk)
                    call.sessions.set(UserSession(user.user, createSession.sessionID!!))
                call.respond(createSession)

            }

            post("json") {
                val data = call.receive<String>()
                println("post json $data")
                val fw = FileWriter(File("resources/jsons/widgets.json"))
                fw.write("{\"widgets\": $data}")
                fw.flush()
                fw.close()

                val f = "{\"widgets\": $data}"
                call.respondText(f)
            }
            post("json_ru") {
                val data = call.receive<String>()
                println("json $data")
                val fw = FileWriter(File("resources/jsons/widgetsRU.json"))
                fw.write("{\"widgets\": $data}")
                fw.flush()
                fw.close()

                val f = "{\"widgets\": $data}"
                call.respondText(f)
            }
        }
    }
}
