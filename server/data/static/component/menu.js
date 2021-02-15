'use strict'
import { h, Fragment } from '/js/preact.js'

function Menu(props){
	return h(Fragment,null,
			h(MenuItem,{href:'/',text:'My Files'}),
			h(MenuItem,{href:'/share/',text:'Search'}),
			h(MenuItem,{href:'/group/',text:'Groups'}),
			h(MenuItem,{href:'/board/',text:'Dashboards'}),
			h(MenuItem,{href:'/ticket/',text:'Tickets'}),
	)
}

function MenuItem(props){
	return h('a',{href:props.href},
			h('img',{src:'/assets/menu/'+props.ico,title:props.text}),		
			h('div',{className:'menu-text'},props.text),		
	)

}
export {Menu}