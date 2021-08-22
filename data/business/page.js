'use strict'
import { h } from '/js/preact.js'
import { useState, useEffect } from '/js/hooks.js'
import { execute } from '/js/wasm.js'
import { Layout, Board, Grid } from '/component/components.js'
/**
 * Creates the homepage for the cryptui project.
 *
 * @return {object} vdom of the page
 */
function Page () {
  const layoutOptions = {
    title: 'CryptUI.de'
  }
  const [user, setUser] = useState()
  const userId = localStorage.getItem('userId')
  useEffect(() => {
    if (userId) {
      fetch('/user/' + userId + '/data.json').then((res) => res.json()).then(setUser)
    }
  }, [true])
  return h(Layout, layoutOptions,
    h(Grid, null,
      h(About),
      h(NewAccount),
      user && h(Board, { title: h('a', { href: '/user/' + userId + '/' }, user.name) },
        h('div', null
        )
      )
    )
  )
}

function NewAccount () {
  const [username, setUsername] = useState('')
  const [createKeyDisabled, setCreateKeyDisabled] = useState()
  const [createKeyMsg, setCreateKeyMsg] = useState('')
  const createUser = () => {
    fetch('/user/', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/user.instance'
      },
      body: JSON.stringify({ name: username, key: localStorage.getItem('pub') })
    }).then((res) => {
      localStorage.setItem('userId', res.headers.get('Id'))
      window.location.href = res.headers.get('Location')
    })
  }

  return h(Board, { title: 'Create account' },
    h('p', null, 'First you need to create your brand new RSA Key-Pair. Then you need to share your Public Key so that we can identify you later on. In addition we want you to provide a sort of username, that can be combined with the public key. Last, we encourage you to store a copy of your private key on a secure place. At the moment it is only stored in the local storage of your browser. In case of lost of your private key, you can not access any of the uploaded files anymore. There is no recover mechanism.'),
    h('input', {
      type: 'button',
      disabled: createKeyDisabled,
      value: 'Create new Key',
      onClick: () => {
        setCreateKeyDisabled(true)
        setCreateKeyMsg('Generating')
        execute({ function: 'createKey' }).then(res => {
          localStorage.setItem('pk', res.pk)
          localStorage.setItem('pub', res.pub)
          setCreateKeyMsg('Finished')
        })
      }
    }),
    createKeyMsg,
    h('div', null, 'Name'),
    h('input', {
      type: 'text',
      value: username,
      onInput: (e) => setUsername(e.target.value)
    }),
    h('input', {
      type: 'button',
      value: 'Submit',
      onClick: createUser
    })
  )
}
function About () {
  return h(Board, { title: 'The only cloud storage with a real end to end encryption' },
    h('p', null, 'Goal of this project is to provide a platform independent file host, where all of your files are getting encrpyted right before you upload it. It aim to be as easy to use as other well known file hoster.'),
    h('p', null, 'The project is open source, so you can easily set up your own file hosting server.'),
    h('a', {
      rel: 'noopener',
      href: 'https://github.com/thomas-hoer/cryptui',
      target: '_blank'
    }, 'Visit us on Github')
  )
}
export { Page }
