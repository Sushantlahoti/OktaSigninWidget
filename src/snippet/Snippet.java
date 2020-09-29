package snippet;

public class Snippet {
	<!doctype html>
	<html lang="en-us">
	
	<head>
	    <meta charset="utf-8">
	    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
	    <meta name="viewport" content="width=device-width, initial-scale=1">
	    <title>Example Okta Sign-In Widget & OIDC Flow</title>
		<H1> Initiate login to get Session token & exchange session token in initiate  OIDC flow to get a session cookie</H1>
	    <script src="https://ok1static.oktacdn.com/assets/js/sdk/okta-signin-widget/1.7.0/js/okta-sign-in.min.js" type="text/javascript"></script>
	    <link href="https://ok1static.oktacdn.com/assets/js/sdk/okta-signin-widget/1.7.0/css/okta-sign-in.min.css" type="text/css"
	        rel="stylesheet">
	    <link href="https://ok1static.oktacdn.com/assets/js/sdk/okta-signin-widget/1.7.0/css/okta-theme.css" type="text/css"
	        rel="stylesheet">
			<script src="https://cdn.jsdelivr.net/npm/es6-promise@4/dist/es6-promise.auto.js"></script>
			<script src="https://unpkg.com/jwt-js-decode@1.3.7/dist/jwt-js-decode.min.js" type="text/javascript"></script>
	</head>
	
	<body>
	    <div id="okta-login-container"></div>
	
	    <script>
	
	        var baseURL = 'https://dev-497881.oktapreview.com'
	        var oktaSignIn = new OktaSignIn({
	            baseUrl: baseURL,
	            redirectUri: "https://dev-497881.oktapreview.com/app/UserHome"
	        });
	        oktaSignIn.renderEl({
	                el: '#okta-login-container'
	            },
	            function (res) {
					
	                console.log('Status:', res);
	                if (res.status === 'SUCCESS') {
						console.log('Authentical Successful. Session Token is :' + res.session.token);
	                    var redirectURL = "http://localhost:8080/OktaSigninWidget/welcome.html";
	                    var sessionToken = res.session.token;
	                    if (sessionToken) {
							//Initiate the OIDC Flow by calling the authorize point/
							//redirectURL need not be a real URL and can be anything. The only requirement is that it should be configured as "Login redirect uri" for the OIDC app
	                        var url = baseURL + "/oauth2/v1/authorize?"+
	                        "client_id=0oalrg3nn9s7TuDAd0h7&redirect_uri="+redirectURL+"&response_type=id_token token&response_mode=okta_post_message&state=sadfadifasasdfs23e424e"+
	                        "&nonce=QwcvZdVdM6&prompt=none&scope=openid" +
	                        "&sessionToken="+ sessionToken ;
	                        callIframe(url);
	                    } else {
	                        console.log("There is no Session Token");
	                    }
	                }
	            },
	            function (err) {
	                console.log('Error: ', err);
	            });
	
	       function callIframe(url) {
	            //console.log("callIframe", url);
	            var id_tokenPromise = this.getWindowListenerPromise();
	            var iframe = document.createElement('iframe');
	            iframe.style.display = 'none';
	            iframe.src = url;
	            document.body.appendChild(iframe);
	            //TODO remove iframe after load - depends on after login process
	            return id_tokenPromise;
	        }
	        function getWindowListenerPromise() {	
	            return new Promise(function (resolve, reject) {
	                window.addEventListener("message", function (event) {
	                    console.log('OIDC Response reveived from Server');
						console.log( event.data);
						//Redirect the user to the page where he came from: {URL} query string for Tenet
						window.location.href='http://localhost:8080/OktaSigninWidget/welcome.html';
	                    if (event.data && event.data.id_token) {
	                        //Write id_token to local storage so that it can be accessed as needed
							localStorage.setItem("id_token", event.data.id_token);
							localStorage.setItem("access_token", event.data.access_token);
							//document.cookie = "IDP_Session="+event.data.id_token; 
							
							//JS Library to read ID_TOKEN DATA
							//***var jwt = jwtJsDecode.jwtDecode(event.data.id_token);
							//***document.cookie = "IDP_Session=ACTIVE;id=" + jwt.payload.id  "expiresAt=" + jwt.payload.exp;
							showMe();
							resolve(event.data);
	                        window.removeEventListener("message", function () {}, false);
	                    } else {
	                        reject("Error!");
	                    }
	                    window.removeEventListener("message", function () {}, false);
	                }, false)
	            });
	        }
			
			function showMe(){
				try
				{
					//console.log('Calling Show Me');
					
					var xhr = new XMLHttpRequest();
					xhr.withCredentials = true;
					xhr.open("GET", "https://dev-497881.oktapreview.com/api/v1/sessions/me",false);
					xhr.setRequestHeader("accept", "application/json");		
					//xhr.setRequestHeader("Content-Type", "application/json");	
					//xhr.setRequestHeader("Cookie", "application/json");
					//xhr.setRequestHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36");	
					xhr.send(null);
					if (xhr.readyState ===4)
					{
					
						if(xhr.status == "200"){
							var exp=JSON.parse(xhr.responseText);
							//var jwt = jwtJsDecode.jwtDecode(ID_Token);
							//var d=exp.expiresAt;
							document.cookie="IDP_SessionID=" + exp.id + ";expires=" + exp.expiresAt ;
							//document.cookie="IDP_expiresAt=" + exp.expiresAt;
							
							//document.getElementById("result").innerHTML='/api/v1/sessions/me call :' + xhr.responseText;	
							//document.getElementById("result").innerHTML='The session expires at '  + exp.expiresAt;
							console.log("IDP_SessionID=" + exp.id + ";IDP_expiresAt=" + exp.expiresAt + ";IDP_createdAt=" + exp.createdAt + ";IDP_UserID=" + exp.userId);
						}		
						else if(xhr.status == "404")
						{
							console.log('/api/v1/sessions/me call :404 not found. Session does not exist or is Invalid.');
							//window.location="http://localhost:8080/login.html?redirectURL=http://localhost:8081/session.html";
						}
						else
						{
							console.log('/api/v1/sessions/me call : Something went wrong : ' + xhr.responseText);
							//window.location="http://localhost:8080/login.html?redirectURL=http://localhost:8081/session.html";
						}
						return;
						
					}
					
					/* var settings = {
					  "async": true,
					  "crossDomain": true,
					  "url": "https://dev-302024.oktapreview.com/api/v1/sessions/me",
					  "method": "GET",
					  "headers": {
						"Accept": "application/json",
						"Content-Type": "application/json",
						"cache-control": "no-cache"
					  },
					  "processData": false,
					  "data": ""
					}
	
					$.ajax(settings).done(function (response) {
					  console.log(response);
					  alert(response);
					}); */
					
				}
					catch (err) {
						console.log(err);
						return false;
					} 
			}
					
					
	    </script>
	</body>
	
	</html>
	
	
}

