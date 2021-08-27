package internship.plugins

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import internship.*
import io.ktor.application.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.text.StringEscapeUtils
import java.io.File
import java.io.FileWriter
import java.util.*
import kotlin.collections.ArrayList

const val address = "http://10.90.138.10"

fun writeFile(file: File, srcPath: String){
    val writer = file.bufferedWriter()
    val prefs = Application::class.java.classLoader.getResourceAsStream(srcPath)
        ?.bufferedReader().use { res -> res?.readText() }

    println("$srcPath $prefs")
    writer.write("" + prefs)
    writer.flush()
    writer.close()
}

val shouldRewriteFiles = File("resources/version.txt").also { child ->
        if (!child.exists()) {
            child.createNewFile()
            val writer = child.bufferedWriter()
            val cl = Application::class.java.`package`.implementationVersion
            println("created jar version")
            writer.write(cl)
            writer.flush()
            writer.close()
            writeFiles()
        } else {
            val version = File("resources/version.txt").readText().toInt()
            val newVersion = Application::class.java.`package`.implementationVersion.toInt()
            if (newVersion > version){
                val writer = child.bufferedWriter()
                writer.write(newVersion.toString())
                writer.flush()
                writer.close()
                writeFiles()
            }
        }
    }



fun writeFiles(){
    File("resources/jsons").also {
        if (!it.exists()) it.mkdirs()
        File(it, "admin.json").also { child ->
            if (!child.exists()) {
                child.createNewFile()
                writeFile(child, "jsons/admin.json")
            }
        }
        File(it, "widgets.json").also { child ->
            child.createNewFile()
            writeFile(child, "jsons/widgets.json")
        }
        File(it, "widgetsRU.json").also { child ->
            child.createNewFile()
            writeFile(child, "jsons/widgetsRU.json")
        }
        File(it, "list.json").also { child ->
            child.createNewFile()
            writeFile(child, "jsons/list.json")
        }
        File(it, "list-ru.json").also { child ->
            child.createNewFile()
            writeFile(child, "jsons/list-ru.json")
        }
    }
}


fun Application.configureRouting() {
    routing {
        intercept(ApplicationCallPipeline.Setup) {
            val path = call.request.path()
            call.application.environment.log.info("intercept: path:$path")
            when (call.request.path()) {
                "/admin" -> {
                    val admin = call.sessions.get<AdminSession>()
                    println(admin.toString())
                    if (admin != null){
                        if (checkAdminLogin(User(admin.login, admin.psw))){
                            call.application.environment.log.info("intercept admin success")
                        } else {
                            call.application.environment.log.info("go to login again; inner")
                            call.respondRedirect("/login", permanent = false)
                            return@intercept finish()
                        }
                    } else {
                        call.application.environment.log.info("go to login again; outer")
                        call.respondRedirect("/login", permanent = false)
                        return@intercept finish()
                    }

                }
                "/" -> {
                    val user = call.sessions.get<UserSession>()
                    if (user == null) {
                        println("intercept: no user -> redirect to login page")
                        call.sessions.clear<UserSession>()
                        call.respondRedirect("$address/otrs/userpage.pl?Action=Logout", permanent = true)
                        return@intercept finish()
                    } else {
                        println("intercept session: sessCust:${user.interfaceSession};userL:${user.customerUser}")
                        /* when(checkUserOtrs(user)){
                             OtrsInterfaceState.EXPIRED -> {
                                 call.sessions.clear<UserSession>()
                                 println("intercept: otrs session expired -> redirect to login page")
                                 call.respondRedirect("http://10.90.138.10/otrs/userpage.pl", permanent = true)
                             }
                             OtrsInterfaceState.VALID -> {
                                 println("intercept: continue with / path")
                             }
                         }*/
                    }
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

        get("/redir") {
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
                call.application.environment.log.info("ticketSearch start")
                val ids = getTicketIds(userSession!!)
                call.application.environment.log.info("ticketSearch finish")
                call.application.environment.log.info("ticketsgetById start")
                var tickets: List<TicketResponse> = try {
                    getTicketsByIds(userSession, ids)
                } catch (e: Exception) {
                    call.application.environment.log.info(e.message)
                    getTicketsByIds(userSession, ids)
                }
                call.application.environment.log.info("ticketsgetById finish")
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
                        title = StringEscapeUtils.escapeXml10(requestData.fieldsValue?.find {
                            it?.type.equals("Topic") || it?.type.equals(
                                "Тема"
                            )
                        }?.value)
                            ?: "npe",
                        customerUser = userSession!!.customerUser,
                        article = Article(
                            subject = StringEscapeUtils.escapeXml10(requestData.widgetName!!),
                            body = StringEscapeUtils.escapeXml10(requestData.fieldsValue?.find {
                                it?.type.equals("Issue") || it?.type.equals(
                                    "Проблема"
                                )
                            }?.value) +
                                    StringEscapeUtils.escapeXml10(
                                        if (!addittionFields.isNullOrEmpty()) {
                                            "\n" + addittionFields.joinToString() + "\n" + requestData.faq_addon
                                        } else "\n" + requestData.faq_addon
                                    ),
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
                if (checkAdminLogin(user)) {
                    println("login success")
                    call.sessions.set(AdminSession(user.user, user.password))
                    call.respond(SessionResponse())
                } else {
                    call.respond(SessionResponse(isOk = false))
                }
            }

            get("cookies/{interfaceSessionId}/{password}/{user}") {
                val interfaceSessionId = call.parameters["interfaceSessionId"]
                val password = call.parameters["password"]
                val login = call.parameters["user"]

                call.application.environment.log.info("session create start")
                val createSession = sessionCreate(login!!, password!!)
                call.application.environment.log.info("session create finish")
                if (createSession.isOk) {
                    call.sessions.set(UserSession(login, createSession.sessionID!!, interfaceSessionId!!))
                }
                println("sessionId ${createSession.sessionID} $login")
                println("interfaceSessionId $interfaceSessionId $login")
                // when running in remote
                call.respondRedirect("$address:81/", true)
                // for local test
                //call.respondRedirect("/", true)
            }

            get("logout/{who}") {
                when (call.parameters["who"]) {
                    "user" -> {
                        with(call.sessions.get<UserSession>()) {
                            if (this != null) {
                                /*call.application.environment.log.info("logout start")
                                logoutOtrs(this@with)
                                call.application.environment.log.info("logout finish")*/
                                call.sessions.clear<UserSession>()
                                call.respondText("{\"logout\": \"user\" }")
                            }
                        }
                    }
                    "admin" -> {
                        call.sessions.clear<AdminSession>()
                        call.respondText("{\"logout\": \"admin\" }")
                    }
                }
            }

            get("admin_name") {
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
