package internship.plugins

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
import org.apache.commons.text.StringEscapeUtils
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.time.Duration
import java.util.*
import kotlin.collections.ArrayList


val jsonsRootFile = File("resources/jsons").also {
    if (!it.exists()) it.mkdirs()
    val adminFile = File(it, "admin.json").also { child ->
        if (!child.exists()){
            child.createNewFile()
            val writer = child.bufferedWriter()
            val prefs = Application::class.java.classLoader.getResourceAsStream("jsons/admin.json")
                ?.bufferedReader().use { res -> res?.readText() }

            println("admin.json $prefs")
            writer.write(""+prefs)
            writer.flush()
            writer.close()
        }
    }
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
        intercept(ApplicationCallPipeline.Setup){
            if (call.request.path() == "/admin"){
                println("intercept admin path")
                val admin = call.sessions.get<AdminSession>()
                println(admin.toString())
                if (admin == null || !checkAdminLogin(User(admin.login, admin.psw))) {
                    println("go to login again")
                    call.respondRedirect("/login", permanent = false)
                    //return@intercept finish()
                } else{
                    println("intercept admin success")
                }
            }
        }
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

        get("/redir"){
            println("redir")
            call.respondRedirect("/admin", permanent = false)
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
                var requestData = RequestData(null, null, ArrayList<Field?>(), ArrayList())
                val multipart = call.receiveMultipart()
                val myType = object : TypeToken<ArrayList<Field?>>() {}.type
                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            when (part.name) {
                                "widgetName" -> requestData.widgetName = part.value
                                "faq_addon" -> requestData.faq_addon = part.value
                                "fieldsValue" -> requestData.fieldsValue =
                                    Gson().fromJson<ArrayList<Field?>>(part.value, myType)
                            }
                        }
                        is PartData.FileItem -> {
                            val filedata = FileData(
                                name = part.originalFileName,
                                extension = part.contentType.toString(),
                                data = Base64.getEncoder().encodeToString(part.streamProvider().readBytes())
                            )
                           // println("${part.name} ${filedata.extension}")
                            requestData.files?.add(filedata)
                        }
                    }
                }

                println("fields values " + requestData.fieldsValue.toString())
                println("faq addon " + requestData.faq_addon)
                val userSession = call.sessions.get<UserSession>()
                val addittionFields = requestData.fieldsValue?.filter {
                    !it?.type.equals("Topic") && !it?.type.equals("Issue") &&
                            !it?.type.equals("Тема") && !it?.type.equals("Проблема")
                }?.map { "${it?.type}: ${it?.value}\n" }
                println("additionalFIles $addittionFields")
                val ticketResp = formTicketCreate(
                    Ticket(
                        title = StringEscapeUtils.escapeXml10(requestData.fieldsValue?.find { it?.type.equals("Topic") || it?.type.equals("Тема") }?.value)
                            ?: "npe",
                        customerUser = userSession!!.customerUser,
                        article = Article(
                            subject = StringEscapeUtils.escapeXml10(requestData.widgetName!!),
                            body = StringEscapeUtils.escapeXml10(requestData.fieldsValue?.find { it?.type.equals("Issue") || it?.type.equals("Проблема") }?.value)  +
                                    StringEscapeUtils.escapeXml10(if (!addittionFields.isNullOrEmpty()) {
                                        "\n" + addittionFields.joinToString() + "\n" + requestData.faq_addon
                                    }
                                    else "\n" + requestData.faq_addon),
                            attachments = requestData.files
                        ),
                    ),
                    userSession.sessionId
                )
                call.respond(ticketResp)
            }

            post("adminlogin") {
                val json = call.receive<String>()
                println("create ses: $json")
                val user = Gson().fromJson(json, User::class.java)
                if (checkAdminLogin(user)){
                    println("login success")
                    call.sessions.set(AdminSession(user.user, user.password))
                    call.respond(SessionResponse())
                } else {
                    call.respond(SessionResponse(isOk = false))
                }

                /*val user = Gson().fromJson(json, User::class.java)
                //val user = call.receiveParameters()
                val createSession = sessionCreate(user.user, user.password)
                // добавить аутентификацию через jsoup
                if (createSession.isOk)
                    call.sessions.set(UserSession(user.user, createSession.sessionID!!, ""))
                call.respond(createSession)*/

            }

            get("cookies/{interfaceSessionId}/{password}/{user}") {
                println(call.parameters.toString())
                val interfaceSessionId = call.parameters["interfaceSessionId"]
                val password = call.parameters["password"]
                val login = call.parameters["user"]

                val createSession = sessionCreate(login!!, password!!)
                if (createSession.isOk) {
                    call.sessions.set(UserSession(login, createSession.sessionID!!, interfaceSessionId!!))
                }
                println("sessionId ${createSession.sessionID} $login")
                println("sessionId $interfaceSessionId $login")
                call.respondRedirect("http://10.90.138.10:81/", true)
            }

            get("logout") {
                call.sessions.clear<UserSession>()
                call.sessions.clear<AdminSession>()
                println("logout " + call.sessions.get<UserSession>().toString())
                println("logout " + call.sessions.get<AdminSession>().toString())
                //call.respondRedirect("http://10.90.138.10:81/", true)
                call.respondRedirect("/", true)
            }

            get("admin_name"){
                println("get admin")
                val adminSession = call.sessions.get<AdminSession>()
                call.respondText("{\"login\": \"${adminSession?.login}\"}")
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
