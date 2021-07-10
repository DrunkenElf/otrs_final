axios.defaults.baseURL = '/api/'

let widgets = new Vue({
    el: '#site',
    data: {
        display: 0,
        widgetsEN: {},
        widgetsRU: {},
        widgets: this.widgetsEN,
        language: 0,
        lng_image: document.location.origin+ "/static/assets/img/toggle-left.png",
        requestStatus: false,
        requestResp: "",
        user: {},
        session: {
            customerUser: "",
            sessionId: "",
        },
        requestData: {
            widgetName: "",
            faq_addon: "",
            fieldsValue: []
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
                this.requestData.faq_addon = "";
                this.faq_list = true;
                this.main_menu = 0
            }
        },
        setRequest() {
            console.log(this.session.sessionId)
            if (this.session.sessionId === "")
                alert("Please, login to create ticket")
            else {
                this.requestData.widgetName = this.widgets[this.display - 1].name
                let data = JSON.stringify(this.requestData);
                axios.post('/createTicket', data, {
                    headers: {
                        'Content-Type': 'application/json;charset=UTF-8'
                    }
                }).then((response) => {
                    console.log(response);
                    this.requestResp = response.data;
                    this.requestStatus = true;
                });
            }
            //this.requestStatus = true;
        },
        isEven(i) {
            return i % 2 === 0;
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
                    arr.push({time: this.user[i].articles[j].createTime, title: this.user[i].title, body: this.user[i].articles[j].body, type: this.user[i].stateType});
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
            this.requestData.faq_addon += "\n"+question+" - "+yes
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
            this.requestData.faq_addon += "\n"+question+" - "+no
        },
        changeLanguage() {
            if (this.language === 0) {
                this.widgets = this.widgetsRU;
                this.language = 1;
                this.lng_image = "/static/assets/img/toggle-right.png"
            }else {
                this.widgets = this.widgetsEN;
                this.language = 0;
                this.lng_image = "/static/assets/img/toggle-left.png"
            }
        }
    },
    created: function () {
        axios.get('/json').then((response) =>{
            this.widgetsEN = response.data.widgets;
            this.session = response.data.session;
            this.widgets = this.widgetsEN;
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
    }
})