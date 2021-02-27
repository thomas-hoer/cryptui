'use strict'
import {h} from '/js/preact.js'
import {useState,useEffect,useRef} from '/js/hooks.js'
import {Layout} from '/component/layout.js'
import {Folder} from '/component/folder.js'

function Page(){
	const filesRef = useRef([])
	const knownFiles = filesRef.current
	const [user,setUser] = useState({})
	const [files,setFiles] = useState(knownFiles)
	useEffect(()=>{
		fetch("data.json").then(res=>res.json()).then(setUser)
		fetch("files").then(res=>res.json()).then(res=>{
			const json = decryptToString(res)
			knownFiles.push(...JSON.parse(json))
			setFiles([...knownFiles])
		})
	},[true])
	const layoutOptions = {
			title:user.name,
	}
	const addFile = f => {
		knownFiles.push(f)
		const enc = encryptString(JSON.stringify(knownFiles))
		fetch("files",{
			method:"PUT",
			body:JSON.stringify(enc)
		})
		setFiles([...knownFiles])
	}
	return h(Layout,layoutOptions,
				h(Folder,{files:files,addFile:addFile})
	)
}
export {Page}