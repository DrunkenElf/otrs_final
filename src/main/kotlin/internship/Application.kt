package internship

import internship.plugins.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.content.*


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")// Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module() {
    configureRouting()
    configureMonitoring()
    configureSession()
    configureSerialization()
    configCORS()
    install(CachingHeaders){
        options { outgoingContent ->
            when(outgoingContent.contentType?.withoutParameters()){
                ContentType.Text.Html -> CachingOptions(cacheControl = CacheControl.NoCache(visibility = CacheControl.Visibility.Public))
                else -> null
            }
        }
    }
}


