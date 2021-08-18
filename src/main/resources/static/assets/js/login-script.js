//axios.defaults.baseURL = '/api/'

let widgets = new Vue({
    el: '#login',
    data: {
        display: 0,
        widgets: {},
        requestStatus: false,
        requestResp: "",
        loginField: "",
        passwordField: "",
        isOk: false
    },
    methods: {
        createSession(){
            let user1 = {
                user: this.loginField,
                password: this.passwordField
            }
            let data = JSON.stringify(user1);
            console.log(data);
            axios.post("/api/adminlogin", data, {
                headers: {
                    'Content-Type': 'application/json;charset=UTF-8'
                }
            }).then((response) => {
                console.log(response);
                //this.isOk = isOk
                this.isOk = response.data.isOk;
                if (!this.isOk){
                    alert("Login Failed if");
                } else {
                    console.log("success");
                    //window.location.href = '/admin'
                    window.location.replace('/admin')
                    //this.redir()
                }
                this.requestResp = response;

            }).catch(function (error){
                console.log(error.response);
                alert("Login Failed");
            })
        },
        redir(){
            axios.get('/redir')
        }
    },
    created: function () {
        axios.get('/api/json').then((response) =>{
            this.widgets = response.data.widgets;
        })
    }
})