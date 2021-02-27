'use strict'
import {h} from '/js/preact.js'
import {useState,useEffect} from '/js/hooks.js'
import {Layout} from '/component/layout.js'
import {Folder} from '/component/folder.js'

function Page(){
	const [user,setUser] = useState({})
	useEffect(()=>{
		fetch("data.json").then(res=>res.json()).then(setUser)
	},[true])
	const layoutOptions = {
			title:user.name,
	}
	return h(Layout,layoutOptions,
				h(Folder)
	)
}
export {Page}