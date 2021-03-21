'use strict'
import { h, Fragment } from '/js/preact.js'

/**
 * Component of the overall layout. Includes header, menu and footer.
 *
 * @param {object} props
 * @return {object} vdom of the component
 */
function Layout (props) {
  let title
  if (props.menu) {
    title = [
      h('div'),
      h('div', null, props.menu.map((m) => h('img', { src: m.icon, onClick: m.action })))
    ]
  } else if (props.backButton && window.history.length > 1) {
    const titleIcon = h('img', {
      className: 'header-back',
      src: '/assets/back.png',
      onClick: () => window.history.back()
    })
    title = [
      h('div', null, titleIcon, props.title),
      h('a', { href: '/profile/' }, 'My Profile')
    ]
  }
  return h(Fragment, null,
    h('div', { className: 'header' }, title),
    h('div', { className: 'body' },
      h('div', { className: 'content-wrapper' }, props.children)
    ),
    h('div', { className: 'footer' })
  )
}

export { Layout }
