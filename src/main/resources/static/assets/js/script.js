axios.defaults.baseURL = '/api/'

let widgets = new Vue({
    el: '#site',
    data: {
        loaded: false,
        link: "",
        iframe: {
            src: "",
        },
        display: 0,
        widgetsEN: {},
        widgetsRU: {},
        interface: [],
        interfaceEN: ["IT Support", "Please Login", "New Ticket", "Opened Tickets", "Resolved Tickets", "All Tickets", "Report Issue", "Number of tickets:", "No", "Yes", "Fill the form to report the issue", "Files", "Report", "Your issue has been successfully sent!", "We will fix it as soon as possible."],
        interfaceRU: ["IT Подержка", "Пожалуйста войдите", "Новый тикет", "Открытые тикеты", "Решенные тикеты", "Все тикеты", "Сообщить о проблеме", "Количество тикетов:", "Нет", "Да", "Заполните форму", "Файлы", "Сообщить", "Ваш запрос отправлен!", "Решим проблему как можно скорее."],
        widgets: this.widgetsEN,
        language: 0,
        lng_image: document.location.origin + "/static/assets/img/toggle-left.png",
        requestStatus: false,
        requestResp: "",
        user: {},
        session: {
            customerUser: "",
            sessionId: "",
        },
        requestData: {
            widgetName: "",
            faq: [],
            fieldsValue: [],
            file: []
        },
        faq_list: true,
        question_id: 0,
        answer_id: 0,
        main_menu: 0,
        ticket_number: 10,
        ticketFirstId: 0,
        pageNumbers: 1,
        pageID: 1,
        tickets: [],
        openTickets: [],
        closedTickets: []
    },
    methods: {
        setDisplay(id) {
            this.display = id;
            this.requestStatus = false;
            if (id !== 0) {
                for (let i = 0; i < this.widgets[id - 1].fields.length; i++) {
                    this.requestData.fieldsValue.push({type: this.widgets[id - 1].fields[i].title.toString(), value: ''});
                }
            }else {
                this.requestData.fieldsValue = [];
                this.requestData.file = [];
                //this.requestData.faq_addon = "";
                this.faq_list = true;
                this.main_menu = 0
            }
        },
        async getSrc(url) {
            const res = await fetch(url, {
                method: 'GET',
                headers: {
                    // Here you can set any headers you want
                    //"X-Frame-Options": "SAMEORIGIN",
                    //"Content-Type": "Access-Control-Allow-Origin",
                    "Referrer Policy": "strict-origin-when-cross-origin",
                    "Access-Control-Allow-Origin": "*",
                    "Content-Security-Policy": "frame-ancestors 'self' "+url,
                    "Host": "10.90.138.10",
                    "X-Frame-Options": "SAMEORIGIN"
                }
            });
            const blob = await res.blob();
            const urlObject = URL.createObjectURL(blob);
            document.querySelector('iframe').setAttribute("src", urlObject)
            this.loaded = true;
        },
        sleep(ms) {
            return new Promise(resolve => setTimeout(resolve, ms));
        },
        goToTicketInfo(ticketNumber){
            //window.location.replace('http://10.90.138.10/otrs/customer.pl?Action=CustomerTicketZoom;TicketNumber='+ticketNumber);
            //window.location.href = 'http://10.90.138.10/otrs/customer.pl?Action=CustomerTicketZoom;TicketNumber='+ticketNumber;
            this.iframe.src = 'http://10.90.138.10/otrs/userpage.pl?Action=CustomerTicketZoom;TicketNumber='+ticketNumber;
            //let url = 'http://10.90.138.10/otrs/customer.pl?Action=CustomerTicketZoom;TicketNumber='+ticketNumber;
            //this.getSrc(url);
            this.loaded = true;
            this.sleep(500).then(() => {
                window.scrollTo({ left: 0, top: document.body.scrollHeight, behavior: "smooth" });
            })
        },
        formAddError(input) {
            input.classList.add('_error');
        },
        formRemoveError(input) {
            input.classList.remove('_error');
        },
        setRequest() {
            let fields = document.querySelectorAll("._req");
            let error = 0;

            for (let i = 0; i < fields.length; i++) {
                const input = fields[i];
                this.formRemoveError(input);
                if (input.value === "") {
                    this.formAddError(input);
                    error++;
                }
            }

            if (error === 0){
                console.log(this.session.sessionId)
                if (this.session.sessionId === "")
                    alert("Please, login to create ticket")
                else {
                    this.requestData.widgetName = this.widgets[this.display - 1].name
                    //let data = JSON.stringify(this.requestData);
                    const formData = new FormData();
                    formData.append('widgetName', this.requestData.widgetName)
                    formData.append('faq', JSON.stringify(this.requestData.faq))
                    formData.append('fieldsValue', JSON.stringify(this.requestData.fieldsValue))
                    for (let i = 0; i < this.requestData.file.length; i++) {
                        formData.append(this.requestData.file[i].name, this.requestData.file[i])
                    }

                    axios.post('/createTicket', formData, {
                        headers: { 'Content-Type': 'multipart/form-data;charset=UTF-8' }
                    }).then((response) => {
                        console.log(response);
                        this.requestResp = response.data;
                        this.requestStatus = true;
                        this.getUserData();
                    });
                    this.requestStatus = true;
                    button.disabled = false;
                }
            }
            /*console.log(this.session.sessionId)
            if (this.session.sessionId === "")
                alert("Please, login to create ticket")
            else {
                this.requestData.widgetName = this.widgets[this.display - 1].name
                //let data = JSON.stringify(this.requestData);
                const formData = new FormData();
                formData.append('widgetName', this.requestData.widgetName)
                formData.append('faq', JSON.stringify(this.requestData.faq))
                formData.append('fieldsValue', JSON.stringify(this.requestData.fieldsValue))
                for (let i = 0; i < this.requestData.file.length; i++) {
                    formData.append(this.requestData.file[i].name, this.requestData.file[i])
                }

                axios.post('/createTicket', formData, {
                    headers: { 'Content-Type': 'multipart/form-data;charset=UTF-8' }
                }).then((response) => {
                    console.log(response);
                    this.requestResp = response.data;
                    this.requestStatus = true;
                });
            }*/
            //this.requestStatus = true;
        },
        isEven(i) {
            return i % 2 === 0;
        },
        isLink(i) {
            let url;
            try {
                url = new URL(i);
            }catch (_) {
                return false;
            }

            return url.protocol === "http:" || url.protocol === "https:"
        },
        addFAQquestion(i) {
            this.requestData.faq = [];
            this.requestData.faq.push(i);
        },
        addFAQanswer(i) {
            this.requestData.faq.push(i);
        },
        setTicketNumber(i) {
            this.ticket_number = i;
            this.ticketFirstId = 0;
            if (this.main_menu === 1) this.pageNumbers = Math.ceil(this.openTickets.length / this.ticket_number);
            else if (this.main_menu === 2) this.pageNumbers = Math.ceil(this.closedTickets.length / this.ticket_number);
            else this.pageNumbers = Math.ceil(this.tickets.length / this.ticket_number);
            this.pageID = 1;
        },
        setPage(i) {
            this.pageID = i;
            this.ticketFirstId = (i - 1) * this.ticket_number;
        },
        getTicketArray() {
            let arr = [];
            for (let i in this.user) {
                for (let j in this.user[i].articles) {
                    arr.push({time: this.user[i].articles[j].createTime, title: this.user[i].title, body: this.user[i].articles[j].body,
                        type: this.user[i].stateType, tic_num: this.user[i].ticketNumber});
                }
            }
            this.pageNumbers = Math.ceil(arr.length / 10);
            return arr;
        },
        getOpenTickets() {
            let arr = [];
            for (let i in this.tickets) {
                if (this.tickets[i].type === 'open') {
                    arr.push({...this.tickets[i]});
                }
            }
            return arr;
        },
        getClosedTickets() {
            let arr = [];
            for (let i in this.tickets) {
                if (this.tickets[i].type === 'closed') {
                    arr.push({...this.tickets[i]});
                }
            }
            return arr;
        },
        setMainMenu(id) {
            this.main_menu = id;
            this.pageID = 1;
            this.ticket_number = 10;
            this.ticketFirstId = 0;
            if (id === 1) this.pageNumbers = Math.ceil(this.openTickets.length / this.ticket_number);
            else if (id === 2) this.pageNumbers = Math.ceil(this.closedTickets.length / this.ticket_number);
            else this.pageNumbers = Math.ceil(this.tickets.length / this.ticket_number);
        },
        getData(data) {
            let line = "";
            for (let i = 0; i < data.length; i++) {
                if (data[i] === ' ' || data[i] === ':' || data[i] === '-') continue;
                line += data[i];
            }
            return line;
        },
        setFAQlist() {
            this.faq_list = true;
            this.question_id = 0;
            this.answer_id = 0;
        },
        setFAQlist(question, yes) {
            //this.requestData.faq += "\n"+question+" - "+yes
            this.requestData.faq.push( ""+question+" - "+yes)
            this.faq_list = true;
            this.question_id = 0;
            this.answer_id = 0;
        },
        setQuestion(id) {
            this.question_id = id;
            this.answer_id = 0;
            this.faq_list = false;
        },
        setNextAnswer() {
            this.answer_id += 1;
            if (this.answer_id >= this.widgets[this.display - 1].faq[this.question_id].answers.length) {
                this.faq_list = true;
            }
        },
        setNextAnswer(question, no) {
            this.answer_id += 1;
            if (this.answer_id >= this.widgets[this.display - 1].faq[this.question_id].answers.length) {
                this.faq_list = true;
            }
            this.requestData.faq.push( ""+question+" - "+no)
        },
        changeLanguage() {
            if (this.language === 0) {
                this.widgets = this.widgetsRU.sort((a,b) => (a.order > b.order) ? 1 : ((b.order > a.order) ? -1 : 0));
                this.language = 1;
                this.lng_image = "/static/assets/img/toggle-right.png";
                this.interface = this.interfaceRU;
            }else {
                this.widgets = this.widgetsEN.sort((a,b) => (a.order > b.order) ? 1 : ((b.order > a.order) ? -1 : 0));
                this.language = 0;
                this.lng_image = "/static/assets/img/toggle-left.png";
                this.interface = this.interfaceEN;
            }
        },
        logOut() {
            this.user = {};
            this.session = {};
            this.tickets = {};
            this.openTickets = {};
            this.closedTickets = {};
            axios.get("/logout/user").then((response) => {
                //window.location.reload();
                let temp = response.data.logout
                console.log(temp)
                if (temp === "user"){
                    window.location.replace('http://10.90.138.10/otrs/userpage.pl?Action=Logout')
                } else {
                    window.location.replace('http://10.90.138.10:81/login')
                }

            })
        },
        onFileSelected(event) {
            this.requestData.file = [];
            for (let i = 0; i < event.target.files.length; i++) {
                this.requestData.file.push(event.target.files[i]);
            }
        },
        getUserData(){
            axios.get('/user').then((response) =>{
                this.user = response.data;
                this.tickets = this.getTicketArray();
                this.openTickets = this.getOpenTickets();
                this.closedTickets = this.getClosedTickets();
            });
        }
    },
    created: function () {
        console.log(window.history.state);
        this.interface = this.interfaceEN;
        //console.log(window.location.history.state);
        //console.log(window.location.history.prototype);
        //window.location.
        axios.get('/json').then((response) =>{
            this.widgetsEN = response.data.widgets;
            this.session = response.data.session;
            this.widgets = this.widgetsEN.sort((a,b) => (a.order > b.order) ? 1 : ((b.order > a.order) ? -1 : 0));
        });
        axios.get('/json_ru').then((response) =>{
            this.widgetsRU = response.data.widgets;
            this.session = response.data.session;
        });
        axios.get('/user').then((response) =>{
            this.user = response.data;
            this.tickets = this.getTicketArray();
            this.openTickets = this.getOpenTickets();
            this.closedTickets = this.getClosedTickets();
        });
    },
    mounted: function (){
        window.setInterval(() => {
            axios.get('/json').then((response) =>{
                this.widgetsEN = response.data.widgets;
                this.widgets = this.widgetsEN;
            });
            axios.get('/json_ru').then((response) =>{
                this.widgetsRU = response.data.widgets;
            });
            axios.get('user').then((response) =>{
                this.user = response.data;
                this.tickets = this.getTicketArray();
                this.openTickets = this.getOpenTickets();
                this.closedTickets = this.getClosedTickets();
            });
        }, 60000);
    }
})