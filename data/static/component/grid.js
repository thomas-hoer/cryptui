'use strict'
import { h } from '/js/preact.js'

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
  return h('div', { className: 'content-grid' }, body)
}

export { Grid }
