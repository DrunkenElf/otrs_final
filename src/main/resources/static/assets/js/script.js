axios.defaults.baseURL = '/api/'

let widgets = new Vue({
    el: '#site',
    data: {
        display: 0,
        widgets: {},
        requestStatus: false,
        requestData: {
            userToken: "123",
            fieldsValue: []
        }
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
            }
        },
        setRequest() {
            let data = JSON.stringify(this.requestData);
            //не понял почему хедер в json
            //axios.post('/123', {headers: {'Content-Type': 'application/json'}, data}).then((response) =>{
            axios.post('/123', data).then((response) =>{
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