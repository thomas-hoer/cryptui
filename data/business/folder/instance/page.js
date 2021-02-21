'use strict'
import {h} from '/js/preact.js'
import {useState,useEffect} from '/js/hooks.js'
import {Layout} from '/component/layout.js'
import {Folder} from '/component/folder.js'

function Page(){
	const [files,setFiles] = useState([])
	const [folder,setFolder] = useState([])
	useEffect(()=>{
		fetch("files").then(res=>res.json()).then(res=>{
			const json = decryptToString(res)
			setFiles(JSON.parse(json))
		})
		fetch("?json").then(res=>res.json()).then(setFolder)
	},[true])
	const splits = window.location.pathname.split("/")
	const layoutOptions = {
			title:splits[splits.length-2],
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
				h(Folder,{files:files,addFile:addFile,folder:folder})
	)
}
export {Page}