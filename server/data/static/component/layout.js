'use strict'
import { h, Fragment } from '/js/preact.js'
import { useState } from '/js/hooks.js'
import { Menu } from '/component/menu.js'
import { Text } from '/component/text.js'

function Layout(props){
	const [serach,setSearch] = useState('')
	const quickSearch = {get:()=>serach, set:setSearch}
	let titleIcon = h('img',{className:'menu-icon',src:'/assets/menu.png'})
	if (props.backButton && window.history.length>1){
		titleIcon = h('img',{className:'header-back',src:'/assets/back.png',onClick:()=>window.history.back()})
	}
	return h(Fragment,null,
			h('div',{className:'header'},
					h('div',null,titleIcon,props.title),
					//h(Text,{property:quickSearch,type:'text',placeholder:'Quick Search'}),
					h('div',null,'Profile'),
					),
			h('div',{className:'body'},
					//h('div',{className:'menu'},h(Menu)),
					h('div',{className:'content-wrapper'},props.children)
			),
			h('div',{className:'footer'})
			)
}

export {Layout}