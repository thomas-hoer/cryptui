'use strict'
import {h} from '/js/preact.js'
import {Layout} from '/component/layout.js'
import {Folder} from '/component/folder.js'

function Page(){
	const splits = window.location.pathname.split("/")
	const layoutOptions = {
			title:splits[splits.length-2],
	}
	return h(Layout,layoutOptions,
				h(Folder)
	)
}
export {Page}