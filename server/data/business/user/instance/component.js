'use strict';
import { h } from '/js/preact.js';
import { useState, useEffect } from '/js/hooks.js'


function Component(props){
	const [data, setData] = useState({})
	useEffect(()=>{
		fetch(props.path+"data.json").then(r=>r.json()).then(setData)
	},[true])

	if (props.style=='small'){
		return h('a',{href:props.path,style:{fontWeight:'bold'}},
				h('img',{src:data.image,style:{float:'left'}}),
				data.name)
	}
	return h('a',{href:props.path,className:'user-box'},
			h('img',{src:data.image}),
			data.name)
}
export {Component}