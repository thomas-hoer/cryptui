'use strict'
import { h } from '/js/preact.js'
import { useState, useEffect } from '/js/hooks.js'
import { Layout } from '/component/layout.js'
import { Folder } from '/component/folder.js'

/**
 * Creates the start page for an user. It also includes the root directoy of
 * the user.
 *
 * @return {object} vdom of the page
 */
function Page () {
  const [user, setUser] = useState({})
  useEffect(() => {
    fetch('data.json').then((res) => res.json()).then(setUser)
  }, [true])
  const layoutOptions = {
    backButton: true,
    title: user.name
  }
  return h(Layout, layoutOptions,
    h(Folder)
  )
}
export { Page }
