axios.defaults.baseURL = '/api/'

let widgets = new Vue({
    el: '#site',
    data: {
        display: 0,
        widgets: {},
        requestStatus: false,
        edit: false,
        fieldTypes: {
            "input": "Input Line",
            "textarea": "Input Field"
        },
        newCategory: {
            id: "",
            name: "",
            description: "",
            icon: "",
            fields: [
                {
                    "title": "",
                    "type": "input",
                    "placeholder": ""
                }
            ]
        },
        requestData: {
            userToken: "123",
            fieldsValue: []
        }
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
            }
        },
        setEdit(id) { this.display = id; this.edit = true; },
        deleteField(id, field) {
            this.widgets[id - 1].fields.splice(this.widgets[id - 1].fields.indexOf(field), 1);
        },
        deleteNewField(field) {
            this.newCategory.fields.splice(this.newCategory.fields.indexOf(field), 1);
        },
        addField(id) {
            this.widgets[id - 1].fields.push({title: '', placeholder: '', type: "input"});
        },
        addNewField() {
            this.newCategory.fields.push({title: '', placeholder: '', type: "input"});
        },
        updateJSON() {
            let data = JSON.stringify(this.widgets);
            axios.post('/json', data).then((response) =>{
            //axios.post('/json', {headers: {'Content-Type': 'application/json'}, data}).then((response) =>{
                console.log(response);
            })
            this.requestStatus = true;
        },
        insertCategory() {
            this.newCategory.id = this.widgets.length + 1;
            this.widgets.push(this.newCategory);
            let data = JSON.stringify(this.widgets);
            this.updateJSON();
        },
        sendRequest() {
            let data = JSON.stringify(this.requestData);
            axios.post('/123', data).then((response) =>{
            //axios.post('/123', {headers: {'Content-Type': 'application/json'}, data}).then((response) =>{
                console.log(response);
            });
            this.requestStatus = true;
        }
    },
    created: function () {
        axios.get('/json').then((response) =>{
            this.widgets = response.data.widgets;
        })
    }
})