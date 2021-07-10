package internship

/*
data class UserSes(
    val login: String,
    val sessionID: String,
)
data class LoginCredentials(
    val link: String,
    val cookies: Map<String, String>,
)
*/
/*data class DynamicField(
    val name: String,
    val value: String,
)

data class Customer(
    val customerUser: String,
    val customerID: String,
)*/
data class Article(
    val subject: String,
    val body: String,
    val contentType: String = "text/plain; charset=utf8"
)

data class Field(
    val type: String?,
    val value: String?
)

data class RequestData(
    val widgetName: String,
    val faq_addon: String?,
    val fieldsValue: ArrayList<Field?>?
)
data class User(
    val user: String,
    val password: String,
)

data class Ticket(
    val title: String,
    val customerUser: String,
    val article: Article,
    //val dynamicFields: List<DynamicField?>?
)

data class CreatedTicketResp(
    val artId: String,
    val ticketId: String,
    val ticketNum: String,
)

data class TicketResponse(
    val age: String?,
    val archiveFlag: String?,
    val articles: List<ArticleResponse?>,
    val changeBy: String?,
    val changed: String?,
    val createBy: String?,
    val created: String?,
    val customerId: String?,
    val customerUserId: String?,
    val firstLock: String?,
    val firstResponse: String?,
    val groupId: String?,
    val lock: String?,
    val lockId: String?,
    val owner: String?,
    val ownerId: String?,
    val responsible: String?,
    val responsibleId: String?,
    val state: String?,
    val stateId: String?,
    val stateType: String?,
    val ticketId: String?,
    val ticketNumber: String?,
    val title: String?,
    val type: String?,
    val typeId: String?,
)

data class ArticleResponse(
    val articleId: String?,
    val articleNumber: String?,
    val bcc: String?,
    val body: String?,
    val cc: String?,
    val changeBy: String?,
    val changeTime: String?,
    val createBy: String?,
    val createTime: String?,
    val from: String?,
    val incomingTime: String?,
    val messageId: String?,
    val subject: String?,
    val ticketId: String?,
    val to: String?,
)

data class SessionResponse(
    val isOk: Boolean = true,
    val sessionID: String? = null,
    val errorMsg: String? = null
)

