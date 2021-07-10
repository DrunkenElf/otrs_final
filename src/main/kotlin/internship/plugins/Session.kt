package internship.plugins

import io.ktor.application.*
import io.ktor.sessions.*

data class UserSession(
    val customerUser: String,
    val sessionId: String
)

fun Application.configureSession(){
    install(Sessions){
        cookie<UserSession>("user_session")
    }
}