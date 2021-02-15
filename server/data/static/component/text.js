'use strict'
import { h } from '/js/preact.js'

function Text(props){
	const setEv = props.type=='number' ? value => props.property.set(parseFloat(value)): props.property.set;
	const input = h('input',{
		value:props.property.get(),
		onChange:(ev)=>setEv(event.target.value),
		type:props.type,
		placeholder:props.placeholder,
	})

	return props.label?h('label',null,h('div',null,props.label),input):input
}

export {Text}