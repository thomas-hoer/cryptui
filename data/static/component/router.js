'use strict'
import { h } from '/js/preact.js'
import { useState ,useEffect} from '/js/hooks.js'


function Router(){
	const [page,setPage] = useState(null)
	const [pathname,setPathname] = useState(window.location.pathname)

	const loadPage = async (pathname) => {
			const module = await import(pathname+"page.js").catch(()=>{setPage(h('div',null,'404'))})
			if(module){
				setPage(h(module.Page))
			}
		}
	useEffect(()=>{
		const interceptClickEvent = function (e) {
			//var target = e.target || e.srcElement;
			for (const target of e.path) {
			if (target.tagName === 'A' && target.noRouter !== true) {
				var href = target.getAttribute('href');
				var hrefTarget = target.getAttribute('target');
				if(!event.ctrlKey &&!hrefTarget){
					e.preventDefault();
					window.history.pushState(null,null,href);
					setPathname(window.location.pathname)
					break
				}
			}
			}
		}
		if (document.addEventListener) {
			document.addEventListener('click', interceptClickEvent);
		} else if (document.attachEvent) {
			document.attachEvent('onclick', interceptClickEvent);
		}
		return ()=>{if (document.addEventListener) {
			document.removeEventListener('click', interceptClickEvent);
		} else if (document.attachEvent) {
			document.detachEvent('onclick', interceptClickEvent);
		}}
	},[true])
	useEffect(()=>{
		const oldpopstate = window.onpopstate
		window.onpopstate = () =>{
			setPathname(window.location.pathname)
		}
		return ()=>{window.onpopstate=oldpopstate}
	},[true])
	useEffect(()=>{
		loadPage(pathname)
	},[pathname])
	return page
}



export {Router}