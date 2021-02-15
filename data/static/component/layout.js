'use strict'
import { h, Fragment } from '/js/preact.js'
import { useState } from '/js/hooks.js'

function Layout(props){
	let titleIcon = h('img',{className:'menu-icon',src:'/assets/menu.png'})
	if (props.backButton && window.history.length>1){
		titleIcon = h('img',{className:'header-back',src:'/assets/back.png',onClick:()=>window.history.back()})
	}
	return h(Fragment,null,
			h('div',{className:'header'},
					h('div',null,titleIcon,props.title),
					h('div',null,'Profile'),
					),
			h('div',{className:'body'},
					h('div',{className:'content-wrapper'},props.children)
			),
			h('div',{className:'footer'})
			)
}

export {Layout}