'use strict';
import {h} from '/js/preact.js';
import {useState,useEffect} from '/js/hooks.js'
import {Layout} from '/component/layout.js';
import {Board} from '/component/board.js'
import {Grid} from '/component/grid.js'

function Page(props){
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
	const toBase64 = file => new Promise((resolve, reject) => {
	    const reader = new FileReader()
	    reader.readAsArrayBuffer(file)
	    reader.onload = () => resolve(reader.result)
	    reader.onerror = error => reject(error)
	})
	const submit = async ev => {
		ev.preventDefault()
		const file = ev.target[0].files[0]
		const result = await toBase64(file)
		const int8Array = new Uint8Array(result)
		const enc = encrypt(int8Array)
		fetch("/files/",{
			method:"POST",
			headers: {
				'Content-Type': 'application/file.instance'
			},
			body:JSON.stringify(enc)
		}).then(res=>{
			const id = res.headers.get('Id')
			const newFiles = [...files,{name:file.name,id:id}]
			const enc = encryptString(JSON.stringify(newFiles))
			fetch("files",{
				method:"PUT",
				body:JSON.stringify(enc)
			})
			setFiles(newFiles)
		})
		return false
	}
	const download = f =>{
		fetch("/files/"+f.id+"/data.json").then(res=>res.json()).then(res=>{
			const str = decryptToBase64(res)
			var a = document.createElement("a")
			a.noRouter = true // needed for router.js
	        a.href =  "data:octet/stream;base64,"+str
	        a.download = f.name
	        a.click()
		})
	}
	const layoutOptions = {
			title:user.name,
	}
	const fileElements = files.map(f=>h('div',{onClick:()=>download(f),className:'file'},f.name))
	return h(Layout,layoutOptions,
			h(Grid,null,
					h(Board,{title:"Upload"},
							h('form',{onsubmit:submit},
								h('input',{type:'file',multiple:'multiple'}),
								h('input',{type:'submit'})
							),
						),
					),
			h(Grid,null,
					...fileElements
					)
	)
}
export {Page}