'use strict'
import { h } from '/js/preact.js'
import { Layout } from '/component/layout.js'
import { Folder } from '/component/folder.js'

/**
 * Creates the page of a folder instance.
 *
 * @return {object} vdom of the page
 */
function Page () {
  const splits = window.location.pathname.split('/')
  const layoutOptions = {
    backButton: true,
    title: splits[splits.length - 2]
  }
  return h(Layout, layoutOptions,
    h(Folder)
  )
}
export { Page }
