package internship.plugins

import io.ktor.application.*
import io.ktor.sessions.*

data class UserSession(
    val customerUser: String,
    val sessionId: String,
    val interfaceSession: String
)
data class AdminSession(
    val login: String,
    val psw: String
)

fun Application.configureSession(){
    install(Sessions){
        cookie<UserSession>("user_session"){
            //cookie.maxAgeInSeconds = 360
        }
        cookie<AdminSession>("admin_session"){
            //cookie.maxAgeInSeconds = 360
        }
    }
}