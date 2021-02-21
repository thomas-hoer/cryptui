'use strict'
import {h} from '/js/preact.js'
import {useState,useEffect} from '/js/hooks.js'
import {Layout} from '/component/layout.js'
import {Board} from '/component/board.js'
import {Grid} from '/component/grid.js'

function Page(){
	const [username,setUsername] = useState('')
	const layoutOptions = {
			title:"CryptUI.de",
	}
	const [user,setUser] = useState()
	const userId = localStorage.getItem('userId')
	useEffect(()=>{
		if(userId){
			fetch("/user/"+userId+"/data.json").then(res=>res.json()).then(setUser)
		}
	},[true])
	const createUser = ()=>{
		fetch("/user/",{
			method:"POST",
			headers: {
				'Content-Type': 'application/user.instance'
			},
			body:JSON.stringify({name:username,key:localStorage.getItem('pub')})
		}).then(res=>{
			localStorage.setItem('userId', res.headers.get('Id'))
			window.location.href=res.headers.get('Location')
		})
	}
	return h(Layout,layoutOptions,
			h(Grid,null,
					h(Board,{title:"Create account"},
						h('p',null,'First you need to create your brand new RSA Key-Pair. Then you need to share your Public Key so that we can identify you later on. In addition we want you to provide a sort of username, that can be combined with the public key. Last, we encourage you to store a copy of your private key on a secure place. At the moment it is only stored in the local storage of your browser. In case of lost of your private key, you can not access any of the uploaded files anymore. There is no recover mechanism.'),
						h('input',{type:'button',value:'Create new Key',onClick:e=>createKey()}),
						h('div',null,'Name'),
						h('input',{type:'text',value:username,onChange:e=>setUsername(e.target.value)}),
						h('input',{type:'button',value:'Submit',onClick:createUser}),
					),
					h(Board,{title:"About the project"},
						h('p',null,'Goal of this project is to provide a platform independent file host, where all of your files are getting encrpyted right before you upload it. It aim to be as easy to use as other well known file hoster.'),
						h('p',null,'The project is open source, so you can easily set up your own file hosting server.'),
						h('a',{href:'https://github.com/thomas-hoer/cryptui',target:'_blank'},'Visit us on Github')
					),
					user && h(Board,{title:h('a',{href:"/user/"+userId+"/"},user.name)},
						h('div',null,
						),
					),
			),
	)
}
export {Page}