'use strict'
import { h } from '/js/preact.js'

function Grid(props){
	let children = props.children || []
	if (!Array.isArray(children)){
		children = [children]
	}
	let body = children.map(c=>h('div',null,c))
	return h('div',{className:'content-grid'},body)
}

export {Grid}