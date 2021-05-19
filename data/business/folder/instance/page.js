'use strict'
import { h } from '/js/preact.js'
import { useState } from '/js/hooks.js'
import { Layout } from '/component/components.js'
import { Folder } from '/component/folder.js'

/**
 * Creates the page of a folder instance.
 *
 * @return {object} vdom of the page
 */
function Page () {
  const [menu, setMenu] = useState()
  const splits = window.location.pathname.split('/')
  const layoutOptions = {
    backButton: true,
    title: splits[splits.length - 2],
    menu: menu
  }
  return h(Layout, layoutOptions,
    h(Folder, { setMenu: setMenu })
  )
}
export { Page }
