'use strict'
import { h } from '/js/preact.js'
import { useState, useEffect } from '/js/hooks.js'
import { Layout } from '/component/layout.js'
import { Board } from '/component/board.js'
import { Grid } from '/component/grid.js'

/**
 * Creates a page for profile and settings.
 *
 * @return {object} vdom of the page
 */
function Page () {
  const [password, setPassword] = useState('')
  const [user, setUser] = useState({})
  const userId = localStorage.getItem('userId')
  if (userId) {
    useEffect(() => {
      fetch('/user/' + userId + '/data.json').then((res) => res.json()).then(setUser)
    }, [true])
  }
  const layoutOptions = {
    backButton: true,
    title: 'Profile'
  }
  const download = (dat) => {
    const str = btoa(dat)
    const a = document.createElement('a')
    a.noRouter = true // needed for router.js
    a.href = 'data:octet/stream;base64,' + str
    a.download = user.name + '.cryptuikey'
    a.click()
  }
  const downloadKey = (ev) => {
    ev.preventDefault()
    const dat = {
      pk: localStorage.getItem('pk'),
      pub: localStorage.getItem('pub'),
      userId: localStorage.getItem('userId')
    }
    const encrypted = encryptAES(JSON.stringify(dat), password)
    download(JSON.stringify(encrypted))
  }
  const toText = (file) => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader()
      reader.readAsText(file)
      reader.onload = () => resolve(reader.result)
      reader.onerror = (error) => reject(error)
    })
  }

  const uploadKey = async (ev) => {
    ev.preventDefault()
    const json = await toText(ev.target[1].files[0])
    const data = JSON.parse(json)
    const keyDataJson = decryptAES(data, password)
    if (keyDataJson !== '') {
      const keyData = JSON.parse(keyDataJson)
      localStorage.setItem('pk', keyData.pk)
      localStorage.setItem('pub', keyData.pub)
      localStorage.setItem('userId', keyData.userId)
      ev.target.reset()
    }
  }

  return h(Layout, layoutOptions,
    h(Grid, null,
      h(Board, { title: 'Keys' },
        userId && h('form', { onsubmit: downloadKey },
          h('div', null, 'Download your private key, so that you can share it between your devices.'),
          h('input', {
            type: 'password',
            placeholder: 'Password',
            value: password,
            onChange: (ev) => setPassword(ev.target.value)
          }),
          h('br'),
          h('input', {
            type: 'submit',
            value: 'Download'
          })
        ),
        userId && h('br'),
        h('form', { onsubmit: uploadKey },
          h('div', null, 'Upload your key.'),
          h('input', {
            type: 'password',
            placeholder: 'Password',
            value: password,
            onChange: (ev) => setPassword(ev.target.value)
          }),
          h('br'),
          h('input', {
            type: 'file',
            placeholder: 'CryptUI Key'
          }),
          h('br'),
          h('input', {
            type: 'submit',
            value: 'Upload'
          })
        )
      )
    )
  )
}
export { Page }
