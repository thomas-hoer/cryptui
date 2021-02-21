'use strict'
import {h,Fragment} from '/js/preact.js'
import {useState} from '/js/hooks.js'
import {Board} from '/component/board.js'
import {Grid} from '/component/grid.js'


function download(f){
	fetch("/files/"+f.id+"/data.json").then(res=>res.json()).then(res=>{
		const str = decryptToBase64(res)
		var a = document.createElement("a")
		a.noRouter = true // needed for router.js
        a.href =  "data:octet/stream;base64,"+str
        a.download = f.name
        a.click()
	})
}
function toBlob(file){
	return new Promise((resolve, reject) => {
	    const reader = new FileReader()
	    reader.readAsArrayBuffer(file)
	    reader.onload = () => resolve(reader.result)
	    reader.onerror = error => reject(error)
	})
}

function Folder(props){
	const [folder,setFolder] = useState('')
	async function submit(ev){
		ev.preventDefault()
		const file = ev.target[0].files[0]
		const result = await toBlob(file)
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
			props.addFile({name:file.name,id:id})
		})
		return false
	}
	function addFolder(ev){
		ev.preventDefault()
		fetch(folder+"/type",{
			method:"PUT",
			body:"folder/instance"
		})
	}
	return h(Fragment,null,
			h(Grid,null,
				h(Board,{title:"Upload"},
					h('form',{onsubmit:submit},
						h('input',{type:'file',multiple:'multiple'}),
						h('input',{type:'submit'})
					)
				),
				h(Board,{title:"New Folder"},
					h('form',{onsubmit:addFolder},
						h('input',{type:'text',value:folder,onChange:ev=>setFolder(ev.target.value)}),
						h('input',{type:'submit'})
					)
				)
			),
			h(Grid,null,
				props.folder.filter(f=>f.includes("/")).map(f=>h('a',{href:f,className:'folder'},f.replace("/","")))
			),
			h(Grid,null,
				props.files.map(f=>h('div',{onClick:()=>download(f),className:'file'},f.name))
			),
		)
}

export {Folder}