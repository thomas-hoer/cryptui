'use strict'
import { h, Fragment } from '/js/preact.js'

/**
 * Creates a board component.
 *
 * @param {object} props
 * @return {object} vdom of the board
 */
function Board (props) {
  let children = props.children || []
  if (!Array.isArray(children)) {
    children = [children]
  }
  let body
  if (props.style === 'list') {
    body = children.map((c) => h('div', { className: 'board-list-element' }, c))
  } else {
    body = [h('div', { className: 'board-element' }, ...children)]
  }
  let footer
  if (props.footer) {
    footer = h('div', { className: 'board-element' }, props.footer)
  }
  return h('div', { className: 'content-board' },
    h('div', { className: 'board-title' },
      h('div', null, props.title),
      props.titleIcon && h('img', {
        src: props.titleIcon,
        onClick: props.iconOnClick,
        alt: props.titleIconAlt
      }),
      props.icons && h('div', null, ...props.icons)
    ),
    h('div', { className: 'board-body' }, ...body, footer)
  )
}

/**
 * Shows a responsive grid design.
 *
 * @param {object} props
 * @return {object} vdom of the component
 */
function Grid (props) {
  let children = props.children || []
  if (!Array.isArray(children)) {
    children = [children]
  }
  const body = children.map((c) => h('div', null, c))
  return h('div', { className: props.className || 'content-grid' }, body)
}

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
      alt: 'Back',
      width: 32,
      height: 32,
      src: '/assets/back.png',
      onClick: () => window.history.back()
    })
    title.push(h('div', null, titleIcon, props.title))
  } else {
    title.push(h('div', null, props.title))
  }
  title.push(props.menu
    ? h('div', null, props.menu.map(menu => h('img', { onClick: menu.action, src: menu.icon })))
    : h('a', { href: '/profile/' }, 'My Profile')
  )
  return h(Fragment, null,
    h('header', null, title),
    h('div', { className: 'body' },
      h('div', { className: 'content-wrapper' }, props.children)
    )
    // h('footer', {})
  )
}

export { Board, Grid, Layout }
