'use strict'
import { h } from '/js/preact.js'

function Board(props){
	let children = props.children || []
	if (!Array.isArray(children)){
		children = [children]
	}
	let body
	if (props.style=="list"){
		body = children.map(c=>h('div',{className:'board-list-element'},c))
	}else{
		body = [h('div',{className:'board-element'},...children)]
	}
	let footer
	if (props.footer){
		footer = h('div',{className:'board-element'},props.footer)
	}
	return h('div',{className:'content-board'},
			h('div',{className:'board-title'},
					h('div',null,props.title),
					props.titleIcon && h('img',{src:props.titleIcon,onClick:props.iconOnClick,alt:props.titleIconAlt}),
					props.icons && h('div',null,...props.icons)
			),
			h('div',{className:'board-body'},...body,footer)
	)
}

export {Board}