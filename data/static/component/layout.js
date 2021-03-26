'use strict'
import { h, Fragment } from '/js/preact.js'

/**
 * Component providing basic elements of the layout. Includes header, menu and
 * footer. It also contains some navigation structure mainly based on the
 * browsers history.
 *
 * @param {object} props
 * @return {object} vdom of the component
 */
function Layout (props) {
  const title = []
  if (props.backButton && window.history.length > 1) {
    const titleIcon = h('img', {
      className: 'header-back',
      src: '/assets/back.png',
      onClick: () => window.history.back()
    })
    title.push(h('div', null, titleIcon, props.title))
  } else {
    title.push(h('div', null, props.title))
  }
  title.push(h('a', { href: '/profile/' }, 'My Profile'))
  return h(Fragment, null,
    h('div', { className: 'header' }, title),
    h('div', { className: 'body' },
      h('div', { className: 'content-wrapper' }, props.children)
    ),
    h('div', { className: 'footer' })
  )
}

export { Layout }
