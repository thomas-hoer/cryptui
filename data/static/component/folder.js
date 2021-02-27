'use strict'
import {h,Fragment} from '/js/preact.js'
import {useState,useEffect} from '/js/hooks.js'
import {Board} from '/component/board.js'
import {Grid} from '/component/grid.js'


function download(f){
	fetch("/files/"+f.id+"/data.json").then(res=>res.json()).then(res=>{
		const str = decryptToBase64(res)
		const a = document.createElement("a")
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
	const [folders,setFolders] = useState([])
	useEffect(()=>{
		fetch("?json").then(res=>res.json()).then(setFolders)
	},[true])
	async function upload(file){
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
			if(file.type.substring(0,5)=="image"){
				const reader = new FileReader()
				reader.readAsDataURL(file)
				reader.onload = function(event) {
					const image = new Image()
					image.src = event.target.result;
					const canvas = document.createElement("canvas")
					canvas.width=300
					canvas.height=150
					const ctx = canvas.getContext('2d');
					image.addEventListener('load', () => {
						ctx.drawImage(image, 0, 0, 300, 150);
						const thumbnail = encryptString(canvas.toDataURL("image/jpeg",0.2))
						fetch("/files/"+id+"/thumb.json",{
							method:"PUT",
							body:JSON.stringify(thumbnail)
						}).then(()=>props.addFile({name:file.name,id:id,thumb:true}))
					})
				}
			}else{
				props.addFile({name:file.name,id:id})
			}
		})
	}
	async function submit(ev){
		ev.preventDefault()
		const files = ev.target[0].files
		for (var i = 0; i < files.length; i++) {
			upload(files[i])
		}
		ev.target.reset()
		return false
	}
	function addFolder(ev){
		ev.preventDefault()
		fetch(folder+"/type",{
			method:"PUT",
			body:"folder/instance"
		}).then(()=>{
			setFolders([...folders,folder+'/'])
			setFolder('')
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
				folders.filter(f=>f.includes("/")).map(f=>h('a',{href:f,className:'folder'},f.replace("/","")))
			),
			h(Grid,null,
				props.files.map(f=>h(ImageComp,{file:f}))
			),
		)
}

function ImageComp(props){
	const [src,setSrc] = useState()
	useEffect(()=>{
		if(props.file.thumb){
			fetch("/files/"+props.file.id+"/thumb.json").then(res=>res.json()).then(res=>setSrc(decryptToString(res)))
		}
	},[true])
	return h('div',{onClick:()=>download(props.file),className:'file'},
		src && h('img',{src:src}),
		props.file.name
	)
}
export {Folder}