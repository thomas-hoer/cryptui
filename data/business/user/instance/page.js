'use strict'
import {h} from '/js/preact.js'
import {useState,useEffect} from '/js/hooks.js'
import {Layout} from '/component/layout.js'
import {Folder} from '/component/folder.js'

function Page(){
	const [user,setUser] = useState({})
	const [files,setFiles] = useState([])
	const [folder,setFolder] = useState([])
	useEffect(()=>{
		fetch("data.json").then(res=>res.json()).then(setUser)
		fetch("files").then(res=>res.json()).then(res=>{
			const json = decryptToString(res)
			setFiles(JSON.parse(json))
		})
		fetch("?json").then(res=>res.json()).then(setFolder)
	},[true])
	const layoutOptions = {
			title:user.name,
	}
	const addFile = f => {
		const newFiles = [...files,f]
		const enc = encryptString(JSON.stringify(newFiles))
		fetch("files",{
			method:"PUT",
			body:JSON.stringify(enc)
		})
		setFiles(newFiles)
	}
	return h(Layout,layoutOptions,
				h(Folder,{files:files,addFile:addFile})
	)
}
export {Page}