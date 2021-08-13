axios.defaults.baseURL = '/api/'

let widgets = new Vue({
    el: '#site',
    data: {
        display: 0,
        widgetsEN: {},
        widgetsRU: {},
        user: {},
        session: {
            customerUser: "",
            sessionId: "",
        },
        admininfo: "",
        widgets: this.widgetsEN,
        language: 0,
        lng_image: "/static/assets/img/toggle-left.png",
        requestStatus: false,
        edit: false,
        main_menu: 0,
        ticket_number: 10,
        ticketFirstId: 0,
        pageNumbers: 1,
        pageID: 1,
        tickets: [],
        openTickets: [],
        closedTickets: [],
        fieldTypes: {
            "input": "Input Line",
            "textarea": "Input Field"
        },
        newCategory: {
            id: "",
            name: "",
            description: "",
            fields: [
                {
                    title: "",
                    type: "input",
                    placeholder: ""
                }
            ],
            faq: [
                {
                    id: 0,
                    question: "Please enter question",
                    answers: []
                }
            ]
        },
        newCategoryRU: {
            id: "",
            name: "",
            description: "",
            fields: [
                {
                    title: "",
                    type: "input",
                    placeholder: ""
                }
            ],
            faq: [
                {
                    id: 0,
                    question: "Введите вопрос",
                    answers: []
                }
            ]
        },
        requestData: {
            userToken: "123",
            fieldsValue: [],
            file: []
        },
        editData: {
            id: "",
            name: "",
            description: "",
            fields: [],
            faq: []
        },
        editDataRU: {
            id: "",
            name: "",
            description: "",
            fields: [],
            faq: []
        },
        faq_list: true,
        question_id: 0,
        answer_id: 0
    },
    methods: {
        setDisplay(id) {
            this.display = id;
            this.requestStatus = false;
            this.edit = false;
            if (id !== 0 && id !== 404) {
                for (let i = 0; i < this.widgets[id - 1].fields.length; i++) {
                    this.requestData.fieldsValue.push({type: this.widgets[id - 1].fields[i].title.toString(), value: ''});
                }
            }else {
                this.requestData.fieldsValue = [];
                this.requestData.file = [];
                this.editData.fields = [];
                this.editDataRU.fields = [];
                this.editData.faq = [];
                this.editDataRU.faq = [];
                this.faq_list = true;
                this.main_menu = 0;
            }
        },
        setEdit(id) {
            this.display = id;
            this.edit = true;
            this.editData.id = id;
            this.editData.name = this.widgetsEN[id - 1].name;
            this.editData.description = this.widgetsEN[id - 1].description;
            this.editData.icon = this.widgetsEN[id - 1].icon;
            for (let i = 0; i < this.widgetsEN[id - 1].fields.length; i++) {
                this.editData.fields.push(this.widgetsEN[id - 1].fields[i]);
            }
            for (let i = 0; i < this.widgetsEN[id - 1].faq.length; i++) {
                this.editData.faq.push({id: this.widgetsEN[id - 1].faq[i].id, question: this.widgetsEN[id - 1].faq[i].question, answers: [...this.widgetsEN[id - 1].faq[i].answers]});
            }
            this.editDataRU.id = id;
            this.editDataRU.name = this.widgetsRU[id - 1].name;
            this.editDataRU.description = this.widgetsRU[id - 1].description;
            this.editDataRU.icon = this.widgetsRU[id - 1].icon;
            for (let i = 0; i < this.widgetsRU[id - 1].fields.length; i++) {
                this.editDataRU.fields.push(this.widgetsRU[id - 1].fields[i]);
            }
            for (let i = 0; i < this.widgetsRU[id - 1].faq.length; i++) {
                this.editDataRU.faq.push({id: this.widgetsRU[id - 1].faq[i].id, question: this.widgetsRU[id - 1].faq[i].question, answers: [...this.widgetsRU[id - 1].faq[i].answers]});
            }
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
        setFAQlist() {
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
        },
        getData(data) {
            let line = "";
            for (let i = 0; i < data.length; i++) {
                if (data[i] === ' ' || data[i] === ':' || data[i] === '-') continue;
                line += data[i];
            }
            return line;
        },
        deleteField(ID) {
            this.editData.fields.splice(ID, 1);
            this.editDataRU.fields.splice(ID, 1);
        },
        deleteNewField(ID) {
            this.newCategory.fields.splice(ID, 1);
            this.newCategoryRU.fields.splice(ID, 1);
        },
        addField() {
            this.editData.fields.push({title: '', placeholder: '', type: "input"});
            this.editDataRU.fields.push({title: '', placeholder: '', type: "input"});
        },
        addNewField() {
            this.newCategory.fields.push({title: '', placeholder: '', type: "input"});
            this.newCategoryRU.fields.push({title: '', placeholder: '', type: "input"});
        },
        updateJSON() {
            let data = JSON.stringify(this.widgets);
            axios.post('/json', data, {
                headers: {
                    'Content-Type': 'application/json;charset=UTF-8'
                }
            }).then((response) =>{
                console.log(response);
                //this.requestStatus = true;
            })
            let dataRU = JSON.stringify(this.widgetsRU);
            axios.post('/json_ru', dataRU, {
                headers: {
                    'Content-Type': 'application/json;charset=UTF-8'
                }
            }).then((response) =>{
                console.log(response);
                this.requestStatus = true;
            });
            //this.requestStatus = true;
            this.imageName = "Choose a file...";
            this.image = null;
        },
        insertCategory() {
            this.newCategory.id = this.widgetsEN.length + 1;
            this.newCategoryRU.id = this.widgetsRU.length + 1;
            this.widgetsEN.push({...this.newCategory});
            for (let i = 0; i < this.newCategory.fields.length; i++) {
                this.newCategoryRU.fields[i].type = this.newCategory.fields[i].type;
            }
            this.widgetsRU.push({...this.newCategoryRU});
            this.updateJSON();
            this.newCategory.id = "";
            this.newCategory.name = "";
            this.newCategory.description = "";
            this.newCategory.icon = "";
            this.newCategory.fields = [{"title": "", "type": "input", "placeholder": ""}];
            this.newCategory.faq = [{id: 0, question: "Please enter the question", answers: []}];
            this.newCategoryRU.id = "";
            this.newCategoryRU.name = "";
            this.newCategoryRU.description = "";
            this.newCategoryRU.icon = "";
            this.newCategoryRU.fields = [{"title": "", "type": "input", "placeholder": ""}]
            this.newCategoryRU.faq = [{id: 0, question: "Введите новый вопрос", answers: []}]
        },
        updateCategory() {
            let index = this.editData.id - 1;
            this.widgetsEN[index].name =  this.editData.name;
            this.widgetsEN[index].description = this.editData.description;
            this.widgetsEN[index].icon = this.editData.icon;
            this.widgetsEN[index].fields = [...this.editData.fields];
            this.widgetsEN[index].faq = [...this.editData.faq];
            this.widgetsRU[index].name =  this.editDataRU.name;
            this.widgetsRU[index].description = this.editDataRU.description;
            this.widgetsRU[index].icon = this.editDataRU.icon;
            for (let i = 0; i < this.editDataRU.fields.length; i++) {
                this.editDataRU.fields[i].type = this.editData.fields[i].type;
            }
            this.widgetsRU[index].fields = [...this.editDataRU.fields];
            this.widgetsRU[index].faq = [...this.editDataRU.faq];
            this.updateJSON();
        },
        deleteCategory(id) {
            this.widgetsRU.splice(id - 1, 1);
            this.widgetsEN.splice(id - 1, 1);
            this.updateJSON();
            this.setDisplay(0);
        },
        sendRequest() {
            let data = JSON.stringify(this.requestData);
            axios.post('/123', {headers: {'Content-Type': 'application/json'}, data}).then((response) =>{
                console.log(response);
            });
            for (let i = 0; i < this.requestData.file.length; i++) {
                const formData = new FormData();
                formData.append('file', this.requestData.file[i]);
                axios.post('/123', {
                    headers: {'Content-Type': 'multipart/form-data'}, formData
                }).then(response => { console.log(response);
                });
            }
            this.requestStatus = true;
        },
        showAnswers(questionID) {
            if (document.getElementById(questionID).style.display === "none") {
                document.getElementById(questionID).style.display = "inline";
            }else {
                document.getElementById(questionID).style.display = "none";
            }
        },
        changeImage(imageID) {
            if (document.getElementById(imageID).src === document.location.origin+ "/static/assets/img/chevron-down.png"){
                document.getElementById(imageID).src = document.location.origin + "/static/assets/img/chevron-up.png";
            }else {
                document.getElementById(imageID).src = document.location.origin + "/static/assets/img/chevron-down.png";
            }
        },
        changeType(ID) {
            if (document.getElementById('h3' + ID).style.display === "") {
                document.getElementById('h3' + ID).style.display = "none";
                document.getElementById('cross' + ID).style.display = "none";
                document.getElementById('input' + ID).style.display = "inline-block";
                document.getElementById('check' + ID).style.display = "block";
            }else {
                document.getElementById('h3' + ID).style.display = "";
                document.getElementById('cross' + ID).style.display = "";
                document.getElementById('input' + ID).style.display = "none";
                document.getElementById('check' + ID).style.display = "none";
            }
        },
        changeTypeRU(ID) {
            if (document.getElementById('h3' + ID).style.display === "") {
                document.getElementById('h3' + ID).style.display = "none";
                document.getElementById('input' + ID).style.display = "inline-block";
                document.getElementById('check' + ID).style.display = "block";
            }else {
                document.getElementById('h3' + ID).style.display = "";
                document.getElementById('input' + ID).style.display = "none";
                document.getElementById('check' + ID).style.display = "none";
            }
        },
        deleteFAQ(ID) {
            this.editData.faq.splice(ID, 1);
            this.editDataRU.faq.splice(ID, 1);
        },
        deleteNewFAQ(ID) {
            this.newCategory.faq.splice(ID, 1);
            this.newCategoryRU.faq.splice(ID, 1);
        },
        deleteAnswer(ID, answerID) {
            this.editData.faq[ID].answers.splice(answerID, 1);
            this.editDataRU.faq[ID].answers.splice(answerID, 1);
        },
        deleteNewAnswer(ID, answerID) {
            this.newCategory.faq[ID].answers.splice(answerID, 1);
            this.newCategoryRU.faq[ID].answers.splice(answerID, 1);
        },
        changeAnswer(ID) {
            if (document.getElementById('h4' + ID).style.display === "") {
                document.getElementById('h4' + ID).style.display = "none";
                document.getElementById('cross' + ID).style.display = "none";
                document.getElementById('input' + ID).style.display = "inline-block";
                document.getElementById('check' + ID).style.display = "block";
            }else {
                document.getElementById('h4' + ID).style.display = "";
                document.getElementById('cross' + ID).style.display = "";
                document.getElementById('input' + ID).style.display = "none";
                document.getElementById('check' + ID).style.display = "none";
            }
        },
        changeAnswerRU(ID) {
            if (document.getElementById('h4' + ID).style.display === "") {
                document.getElementById('h4' + ID).style.display = "none";
                document.getElementById('input' + ID).style.display = "inline-block";
                document.getElementById('check' + ID).style.display = "block";
            }else {
                document.getElementById('h4' + ID).style.display = "";
                document.getElementById('input' + ID).style.display = "none";
                document.getElementById('check' + ID).style.display = "none";
            }
        },
        addNewFAQ() {
            this.editData.faq.push({id: this.editData.faq.length, question: "Please enter question", answers: []});
            this.editDataRU.faq.push({id: this.editDataRU.faq.length, question: "Пожалуйста введите вопрос", answers: []});
        },
        addNewFAQforNew() {
            this.newCategory.faq.push({id: this.newCategory.faq.length, question: "Please enter question", answers: []});
            this.newCategoryRU.faq.push({id: this.newCategoryRU.faq.length, question: "Введите новый вопрос", answers: []});
        },
        addNewAnswer(ID) {
            this.editData.faq[ID].answers.push("Enter new answer");
            this.editDataRU.faq[ID].answers.push("Введите новый вопрос");
        },
        addNewAnswerForNew(ID) {
            this.newCategory.faq[ID].answers.push("Enter new answer");
            this.newCategoryRU.faq[ID].answers.push("Введите новый вопрос");
        },
        logOut() {
            this.user = {};
            this.admininfo = "";
            axios.get('/logout').then((response) => {
                console.log(response);
                //window.location.reload();
            })
        },
        onFileSelected(event) {
            for (let i = 0; i < event.target.files.length; i++) {
                this.requestData.file.push(event.target.files[i]);
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
        axios.get('/admin_name').then((response) => {
            console.log(response);
            this.admininfo = response.data.login;
        })
    }
})