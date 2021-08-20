package internship

import com.google.gson.Gson
import internship.plugins.UserSession
import internship.plugins.address
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.io.File
import java.io.InputStream
import io.ktor.application.*

enum class OtrsInterfaceState{
    VALID,
    EXPIRED
}

fun File.copyInputStreamToFile(inputstream: InputStream) {
    this.outputStream().use { fileOut ->
        inputstream.copyTo(fileOut)
    }
}

suspend fun checkUserOtrs(session: UserSession): OtrsInterfaceState{
    println("checkUserOtrs started")
    val resp = Jsoup.connect("$address/otrs/customer.pl")
        .cookie("OTRSCustomerInterface", session.interfaceSession)
        .method(Connection.Method.GET)
        .execute()
    println("checkUserOtrs finished")
    println("checkUserOtrs ${resp.statusMessage()} ${resp.statusCode()}")
    println("checkUserOtrs ${resp.body()}")
    with(resp.parse()){
        if (this.selectFirst("div.ErrorBox")?.text() != null){
            return OtrsInterfaceState.EXPIRED
        } else return OtrsInterfaceState.VALID
    }
}

fun checkAdminLogin(user: User): Boolean{
    val txt = File("resources/jsons/admin.json").readText()
    println("check admin $txt")
    val admin = Gson().fromJson(txt, Admin::class.java)
    println("check admin: \nadmin: ${admin.login} ${admin.password}\n user: ${user.user} ${user.password}")
    return user.user == admin.login && user.password == admin.password
}


fun logoutOtrs(session: UserSession){
    println("logoutOtrs cookie ${session.interfaceSession}")
    val doc = Jsoup
        .connect("$address/otrs/userpage.pl?Action=Logout")
        .method(Connection.Method.GET)
        .data("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .data("Accept-Encoding","gzip, deflate")
        .data("Accept-Language", "en-GB,en;q=0.9")
        //.data("Access-Control-Allow-Origin", "*")
        .data("Connection", "keep-alive")
        .data("Upgrade-Insecure-Requests", "1")
        //.data("Cookie", "OTRSBrowserHasCookie=1;OTRSCustomerInterface=${session.interfaceSession}")
        .cookie("OTRSCustomerInterface", session.interfaceSession)
        .followRedirects(true)
        .execute()

    doc.headers().forEach { (t, u) ->
        println("$t --- $u")
    }
    println("logoutOtrs ${doc.parse().selectFirst("div.Login.ARIARoleMain")?.html()}")
    println("logoutOtrs ${doc.statusMessage()}")
}

fun getTicketsByIds(session: UserSession, ids: List<String>): List<TicketResponse> {
    val str1 = "<?xml version='1.0' encoding='UTF-8'?>" +
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
            "xmlns:tic=\"http://www.otrs.org/TicketConnector/\">" +
            "<soapenv:Body>\n" +
            "   <tic:TicketGet>\n" +
            "       <SessionID>${session.sessionId}</SessionID>\n" +
            "       <TicketID>${ids.toString().replace("[", "").replace("]", "")}</TicketID>\n" +
            "       <Extended>1</Extended>\n" +
            "       <AllArticles>1</AllArticles>\n" +
            "       <ArticleLimit>10</ArticleLimit>\n" +
            "   </tic:TicketGet>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>"

    val doc = Jsoup
        .connect("$address/otrs/nph-genericinterface.pl/Webservice/webservice_soap")
        .method(Connection.Method.POST)
        .header("SOAPAction", "http://www.otrs.org/TicketConnector/TicketGet")
        .data("ContentType", "text/xml; charset=\"utf-8\"")
        .data("Accept", "text/xml")
        .requestBody(str1)
        .execute()

    val xmlbody = Jsoup.parse(doc.body(), "", Parser.xmlParser())

    val tickets = xmlbody.select("Ticket").map { ticket ->
        TicketResponse(
            age = ticket.selectFirst("Age").text(),
            archiveFlag = ticket.selectFirst("ArchiveFlag").text(),
            articles = ticket.select("Article").map {
                ArticleResponse(
                    articleId = it.selectFirst("ArticleID")?.text(),
                    articleNumber = it.selectFirst("ArticleNumber")?.text(),
                    bcc = it.selectFirst("Bcc")?.text(),
                    body = it.selectFirst("Body")?.text(),
                    cc = it.selectFirst("Cc")?.text(),
                    changeBy = it.selectFirst("ChangeBy")?.text(),
                    changeTime = it.selectFirst("ChangeTime")?.text(),
                    createBy = it.selectFirst("CreateBy")?.text(),
                    createTime = it.selectFirst("CreateTime")?.text(),
                    from = it.selectFirst("From")?.text(),
                    incomingTime = it.selectFirst("IncomingTime")?.text(),
                    messageId = it.selectFirst("MessageID")?.text(),
                    subject = it.selectFirst("Subject")?.text(),
                    ticketId = it.selectFirst("TicketID")?.text(),
                    to = it.selectFirst("To")?.text()
                )
            },
            changeBy = ticket.selectFirst("ChangeBy")?.text(),
            changed = ticket.selectFirst("Changed")?.text(),
            createBy = ticket.selectFirst("CreateBy")?.text(),
            created = ticket.selectFirst("Created")?.text(),
            customerId = ticket.selectFirst("CustomerID")?.text(),
            customerUserId = ticket.selectFirst("CustomerUserID")?.text(),
            firstLock = ticket.selectFirst("FirstLock")?.text(),
            firstResponse = ticket.selectFirst("FirstResponse")?.text(),
            groupId = ticket.selectFirst("GroupID")?.text(),
            lock = ticket.selectFirst("Lock")?.text(),
            lockId = ticket.selectFirst("LockedID")?.text(),
            owner = ticket.selectFirst("Owner")?.text(),
            ownerId = ticket.selectFirst("OwnerID")?.text(),
            responsible = ticket.selectFirst("Responsible")?.text(),
            responsibleId = ticket.selectFirst("ResponsibleID")?.text(),
            state = ticket.selectFirst("State")?.text(),
            stateId = ticket.selectFirst("StateID")?.text(),
            stateType = ticket.selectFirst("StateType")?.text(),
            ticketId = ticket.selectFirst("TicketID")?.text(),
            ticketNumber = ticket.selectFirst("TicketNumber")?.text(),
            title = ticket.selectFirst("Title")?.text(),
            type = ticket.selectFirst("Type")?.text(),
            typeId = ticket.selectFirst("TypeID")?.text()
        )
    }

    return tickets
}

fun getTicket(session: UserSession, id: String): TicketResponse {
    val str = "<?xml version='1.0' encoding='UTF-8'?>" +
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
            "xmlns:tic=\"http://www.otrs.org/TicketConnector/\">" +
            "<soapenv:Body>\n" +
            "   <tic:TicketGet>\n" +
            "       <SessionID>${session.sessionId}</SessionID>\n" +
            "       <TicketID>$id</TicketID>\n" +
            "       <Extended>1</Extended>\n" +
            "       <AllArticles>1</AllArticles>\n" +
            "       <ArticleLimit>10</ArticleLimit>\n" +
            "   </tic:TicketGet>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>"

    val req = Jsoup
        .connect("$address/otrs/nph-genericinterface.pl/Webservice/webservice_soap")
        .method(Connection.Method.POST)
        .header("SOAPAction", "http://www.otrs.org/TicketConnector/TicketGet")
        .data("ContentType", "text/xml; charset=\"utf-8\"")
        .data("Accept", "text/xml")
        .requestBody(str)
        .execute()

    val xmlbody = Jsoup.parse(req.body(), "", Parser.xmlParser())
    val ticket = xmlbody.select("Ticket").first()
    val ticketResponse = TicketResponse(
        age = ticket.selectFirst("Age").text(),
        archiveFlag = ticket.selectFirst("ArchiveFlag").text(),
        articles = ticket.select("Article").map {
            ArticleResponse(
                articleId = it.selectFirst("ArticleID")?.text(),
                articleNumber = it.selectFirst("ArticleNumber")?.text(),
                bcc = it.selectFirst("Bcc")?.text(),
                body = it.selectFirst("Body")?.text(),
                cc = it.selectFirst("Cc")?.text(),
                changeBy = it.selectFirst("ChangeBy")?.text(),
                changeTime = it.selectFirst("ChangeTime")?.text(),
                createBy = it.selectFirst("CreateBy")?.text(),
                createTime = it.selectFirst("CreateTime")?.text(),
                from = it.selectFirst("From")?.text(),
                incomingTime = it.selectFirst("IncomingTime")?.text(),
                messageId = it.selectFirst("MessageID")?.text(),
                subject = it.selectFirst("Subject")?.text(),
                ticketId = it.selectFirst("TicketID")?.text(),
                to = it.selectFirst("To")?.text()
            )
        },
        changeBy = ticket.selectFirst("ChangeBy")?.text(),
        changed = ticket.selectFirst("Changed")?.text(),
        createBy = ticket.selectFirst("CreateBy")?.text(),
        created = ticket.selectFirst("Created")?.text(),
        customerId = ticket.selectFirst("CustomerID")?.text(),
        customerUserId = ticket.selectFirst("CustomerUserID")?.text(),
        firstLock = ticket.selectFirst("FirstLock")?.text(),
        firstResponse = ticket.selectFirst("FirstResponse")?.text(),
        groupId = ticket.selectFirst("GroupID")?.text(),
        lock = ticket.selectFirst("Lock")?.text(),
        lockId = ticket.selectFirst("LockedID")?.text(),
        owner = ticket.selectFirst("Owner")?.text(),
        ownerId = ticket.selectFirst("OwnerID")?.text(),
        responsible = ticket.selectFirst("Responsible")?.text(),
        responsibleId = ticket.selectFirst("ResponsibleID")?.text(),
        state = ticket.selectFirst("State")?.text(),
        stateId = ticket.selectFirst("StateID")?.text(),
        stateType = ticket.selectFirst("StateType")?.text(),
        ticketId = ticket.selectFirst("TicketID")?.text(),
        ticketNumber = ticket.selectFirst("TicketNumber")?.text(),
        title = ticket.selectFirst("Title")?.text(),
        type = ticket.selectFirst("Type")?.text(),
        typeId = ticket.selectFirst("TypeID")?.text()
    )
    return ticketResponse
}

fun getTicketIds(session: UserSession): List<String> {

    val str = "<?xml version='1.0' encoding='UTF-8'?>" +
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
            "xmlns:tic=\"http://www.otrs.org/TicketConnector/\">" +
            "<soapenv:Body>\n" +
            "   <tic:TicketSearch>\n" +
            "       <SessionID>${session.sessionId}</SessionID>\n" +
            "       <Result>ARRAY</Result>\n" +
            "   </tic:TicketSearch>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>"

    val doc = Jsoup
        .connect("$address/otrs/nph-genericinterface.pl/Webservice/webservice_soap")
        .method(Connection.Method.POST)
        .header("SOAPAction", "http://www.otrs.org/TicketConnector/TicketSearch")
        .data("ContentType", "text/xml; charset=\"utf-8\"")
        .data("Accept", "text/xml")
        .requestBody(str)
        .timeout(20000)
        .execute()

    val els = Jsoup.parse(doc.body(), "", Parser.xmlParser())
    val ids = els.select("TicketID")

    val list = ids.map { it.text() }

    return list
}

fun formTicketCreate(ticket: Ticket, sessionId: String): CreatedTicketResp {
    /*val dynaFields = StringBuilder()
    if (ticket.dynamicFields != null)
        ticket.dynamicFields.forEach { field ->
            dynaFields
                .append("<DynamicField>\n")
                .append("   <Name>${field?.name}</Name>\n")
                .append("   <Value>${field?.value}</Value>\n")
                .append("</DynamicField>\n")
        }*/
    println("article body:${ticket.article.body}")
    val attachms = ticket.article.attachments?.map {
        "         <Attachment>\n" +
                "             <Content>${it?.data}</Content>\n" +
                "             <ContentType>${it?.extension}</ContentType>\n" +
                "             <Filename>${it?.name}</Filename>\n" +
                "         </Attachment>"
    }
    val str = "<?xml version='1.0' encoding='UTF-8'?>" +
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
            "xmlns:tic=\"http://www.otrs.org/TicketConnector/\">" +
            "<soapenv:Body>\n" +
            "      <tic:TicketCreate>\n" +
            "      <SessionID>${sessionId}</SessionID>\n" +
            "      <Ticket>\n" +
            "                <Title>${ticket.title}</Title>\n" +
            "                <CustomerUser>${ticket.customerUser}</CustomerUser>\n" +
            "                <QueueID>1</QueueID>\n" +
            "                <StateID>4</StateID>\n" +
            "                <PriorityID>3</PriorityID>\n" +
            "         </Ticket>\n" +
            "         <Article>\n" +
            "             <Subject>${ticket.article.subject}</Subject>\n" +
            "             <Body>${ticket.article.body}</Body>\n" +
            "             <ContentType>${ticket.article.contentType}</ContentType>\n" +
            "         </Article>\n" +
            attachms?.joinToString() +
            // "${if (ticket.dynamicFields != null) dynaFields.toString() else ""}" +
            "      </tic:TicketCreate>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>"

    println("soap create Ticket: $str")
    var doc: Connection.Response? = null
    try {
        doc = Jsoup
            .connect("$address/otrs/nph-genericinterface.pl/Webservice/webservice_soap")
            .method(Connection.Method.POST)
            .header("SOAPAction", "http://www.otrs.org/TicketConnector/TicketCreate")
            .data("ContentType", "text/xml; charset=\"utf-8\"")
            .data("Accept", "text/xml")
            .requestBody(str)
            .execute()
    } catch (e: Error) {
        println(e.message)
        e.printStackTrace()
    }

    println("response: ${doc?.body()}")

    val soap = Jsoup.parse(doc?.body(), "", Parser.xmlParser())
    val artId = soap.select("ArticleID").text()
    val ticketId = soap.select("TicketID").text()
    val ticketNum = soap.select("TicketNumber").text()
    val created = CreatedTicketResp(artId, ticketId, ticketNum)
    return created
}


fun sessionCreate(username: String, password: String): SessionResponse {
    val str = "<?xml version='1.0' encoding='UTF-8'?>" +
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
            "xmlns:tic=\"http://www.otrs.org/TicketConnector/\">" +
            "<soapenv:Body>\n" +
            "   <tic:SessionCreate>\n" +
            "       <CustomerUserLogin>$username</CustomerUserLogin>\n" +
            "       <Password>$password</Password>\n" +
            "   </tic:SessionCreate>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>"
    val resp = Jsoup
        .connect("$address/otrs/nph-genericinterface.pl/Webservice/webservice_soap")
        .method(Connection.Method.POST)
        .header("SOAPAction", "http://www.otrs.org/TicketConnector/SessionCreate")
        .data("ContentType", "text/xml; charset=\"utf-8\"")
        .data("Accept", "text/xml")
        .requestBody(str)
        .execute()

    val doc = Jsoup.parse(resp.body(), "", Parser.xmlParser())
    //println("createSess: ${doc.html()}")
    val msg = doc.select("SessionCreateResponse")
    if (msg.html().contains("Error"))
        return SessionResponse(
            isOk = false,
            errorMsg = msg.select("ErrorMessage").text()
        )
    else
        return SessionResponse(
            sessionID = msg.select("SessionID").text()
        )
}


