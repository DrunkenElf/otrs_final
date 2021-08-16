package internship.plugins

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*

fun Application.configCORS(){
    install(CORS){
        method(HttpMethod.Options)
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        header(HttpHeaders.Authorization)
        header(HttpHeaders.AccessControlAllowHeaders)
        header(HttpHeaders.AccessControlAllowOrigin)
        header(HttpHeaders.XForwardedProto)
        allowSameOrigin = true
        allowNonSimpleContentTypes = true
        allowCredentials = true
        //exposeHeader("Access-Control-Allow-Origin")
        anyHost()
    }
}