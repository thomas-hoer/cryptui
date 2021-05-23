'use strict'
import { h, Fragment } from '/js/preact.js'
import { useState, useEffect, useRef } from '/js/hooks.js'
import { Board, Grid } from '/component/components.js'
import { decryptToBase64, encryptString } from '/component/wasm.js'
import { execute } from '/component/wasm2.js'

function signAndSend (body, method, location, contentType) {
  const data = {
    function: 'sign',
    key: localStorage.getItem('pk'),
    data: body
  }
  return execute(data).then(out => {
    return fetch(location, {
      method: method,
      body: out.data,
      headers: {
        'User-Id': localStorage.getItem('userId'),
        'Content-Type': contentType,
        signature: out.sign
      }
    })
  })
}

const download = (f) => {
  fetch('/files/' + f.id + '/data.json').then((res) => res.json()).then(decryptToBase64).then(str => {
    const a = document.createElement('a')
    a.noRouter = true // needed for router.js
    a.href = 'data:octet/stream;base64,' + str
    a.download = f.name
    a.click()
  })
}

const toBlob = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsArrayBuffer(file)
    reader.onload = () => resolve(reader.result)
    reader.onerror = (error) => reject(error)
  })
}

const upload = async (file, userId) => {
  const result = await toBlob(file)
  const int8Array = new Uint8Array(result)
  return execute({ key: localStorage.getItem('pk'), plain: int8Array }).then(enc => {
    const body = JSON.stringify({ data: enc.data, key: enc.datakey, nonce: enc.nonce })
    return signAndSend(body, 'POST', '/files/', 'application/file.instance')
  })
    .then((res) => {
      return { id: res.headers.get('Id'), file: file }
    })
}

const uploadFiles = (ev, afterUpload, addUpload, userId) => {
  ev.preventDefault()
  const files = ev.target[0].files
  const fileList = []
  for (let i = 0; i < files.length; i++) {
    fileList.push(files[i])
  }
  const uploadThread = async () => {
    for (let file = fileList.pop(); file; file = fileList.pop()) {
      const uploadElement = { name: file.name }
      addUpload(uploadElement)
      await upload(file, userId).then(e => {
        uploadElement.upload = true
        afterUpload(e)
      })
    }
  }
  for (let i = 0; i < Math.min(files.length, 4); i++) {
    uploadThread()
  }
  ev.target.reset()
}

const createThumbnail = (id, file) => {
  return new Promise((resolve) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = (event) => {
      const image = new Image()
      image.src = event.target.result
      const canvas = document.createElement('canvas')
      canvas.width = 300
      canvas.height = 150
      const ctx = canvas.getContext('2d')
      image.addEventListener('load', () => {
        const iw = image.width
        const ih = image.height
        if (ih * 2 > iw) {
          const h = 300 * ih / iw
          ctx.drawImage(image, 0, -(h - 150) / 4, 300, h)
        } else {
          const w = 150 * iw / ih
          ctx.drawImage(image, -(w - 300) / 2, 0, w, 150)
        }
        const dataUrl = canvas.toDataURL('image/jpeg', 0.2)
        localStorage.setItem(id, dataUrl)
        encryptString(dataUrl).then(thumbnail => {
          const body = JSON.stringify(thumbnail)
          signAndSend(body, 'PUT', '/files/' + id + '/thumb.json').then(resolve)
        })
      })
    }
  })
}

function loadAndDecrypt (name, then) {
  const path = window.location.pathname + name
  const request = indexedDB.open('db', 1)
  request.onupgradeneeded = ev => {
    const db = ev.target.result
    db.createObjectStore('data', { keyPath: 'path', autoIncrement: false })
  }
  request.onsuccess = ev => {
    const db = ev.target.result
    const transaction = db.transaction('data', 'readonly')
    const store = transaction.objectStore('data')
    const objectStoreRequest = store.get(path)
    objectStoreRequest.onsuccess = ev => ev.target.result && then(ev.target.result.data)
  }
  fetch('files')
    .then(res => res.json())
    .then(res => execute({ function: 'decryptToString', key: localStorage.getItem('pk'), data: res }))
    .then(out => then(JSON.parse(out.plain)))
}
function saveAndEncrypt (name, data) {
  const path = window.location.pathname + name
  const request = indexedDB.open('db', 1)
  request.onupgradeneeded = ev => {
    const db = ev.target.result
    db.createObjectStore('data', { keyPath: 'path', autoIncrement: false })
  }
  request.onsuccess = ev => {
    const db = ev.target.result
    const transaction = db.transaction('data', 'readwrite')
    const store = transaction.objectStore('data')
    store.put({ path: path, data: data })
  }
}

/**
 * Creates a component that lists all files in the current directory relative
 * to the window state.
 *
 * @param {*} props
 * @return {object} vdom of the component
 */
function Folder (props) {
  const userId = localStorage.getItem('userId')
  const filesRef = useRef([])
  const knownFiles = filesRef.current
  const [files, setFiles] = useState(knownFiles)
  const [newFolderName, setNewFolderName] = useState('')
  const [folders, setFolders] = useState([])
  const uploadListRef = useRef([])
  const [uploadList, setUploadList] = useState([])
  const [selected, setSelected] = useState({})
  useEffect(() => {
    loadAndDecrypt('files', json => {
      knownFiles.length = 0
      knownFiles.push(...json)
      setFiles([...knownFiles])
    })
    fetch('?json').then((res) => res.json()).then(setFolders)
  }, [true])

  const clearUploadList = () => {
    uploadListRef.current = uploadListRef.current.filter(el => !el.upload)
    setUploadList([...uploadListRef.current])
  }
  const addUpload = up => {
    uploadListRef.current.push(up)
    setUploadList([...uploadListRef.current])
  }
  const addFile = f => {
    knownFiles.push(f)
    encryptString(JSON.stringify(knownFiles)).then(enc => {
      const body = JSON.stringify(enc)
      signAndSend(body, 'PUT', 'files')

      saveAndEncrypt('files', [...knownFiles])
      setFiles([...knownFiles])
    })
  }
  const afterUpload = ({ id, file }) => {
    if (file.type.substring(0, 5) === 'image') {
      createThumbnail(id, file)
        .then(() => addFile({ name: file.name, id: id, thumb: true }))
    } else {
      addFile({ name: file.name, id: id })
    }
  }
  const addFolder = async ev => {
    ev.preventDefault()
    signAndSend('folder/instance', 'PUT', newFolderName + '/type').then(() => {
      setFolders([...folders, newFolderName + '/'])
      setNewFolderName('')
    })
  }
  const menu = [{
    icon: '/assets/delete.png',
    action: async () => {
      filesRef.current = filesRef.current.filter((f, i) => !selected[i])
      const enc = await encryptString(JSON.stringify(filesRef.current))
      const body = JSON.stringify(enc)
      signAndSend(body, 'PUT', 'files')
      setFiles([...filesRef.current])
      props.setMenu(undefined)
      setSelected({})
    }
  },
  {
    icon: '/assets/abort.png',
    action: () => {
      props.setMenu(undefined)
      setSelected({})
    }
  }]
  return h(Fragment, null,
    h(Grid, null,
      h(Board, { title: 'Upload' },
        h('form', { onsubmit: (ev) => uploadFiles(ev, afterUpload, addUpload, userId) },
          h('input', { type: 'file', multiple: 'multiple' }),
          h('input', { type: 'submit' })
        )
      ),
      h(Board, { title: 'New Folder' },
        h('form', { onsubmit: addFolder },
          h('input', {
            type: 'text',
            value: newFolderName,
            onInput: (ev) => setNewFolderName(ev.target.value)
          }),
          h('input', { type: 'submit' })
        )
      )
    ),
    h(Grid, { className: 'folder-grid' },
      folders
        .filter((f) => f.includes('/'))
        .map((f) => h('a', {
          href: f,
          className: 'folder'
        }, f.replace('/', '')))
    ),
    h(Grid, { className: 'folder-grid' },
      files.map((f, i) => h(ImageComp, {
        key: f.name,
        file: f,
        select: () => {
          if (selected[i]) {
            delete selected[i]
            if (Object.keys(selected).length === 0) {
              props.setMenu(undefined)
            }
          } else {
            selected[i] = true
            props.setMenu(menu)
          }
          setSelected({ ...selected })
        },
        isSelected: selected[i],
        isSelection: Object.keys(selected).length !== 0
      }))
    ),
    uploadList.length === 0
      ? null
      : h('div', { className: 'notification' },
        ...uploadList.map(f => h('div', null,
          h('img', { src: f.upload ? '/assets/ok.png' : '/assets/loading.png' }),
          f.name
        )),
        h('div', { className: 'button', onclick: clearUploadList }, 'Clear')
      )
  )
}
/**
 * Shows a thumbnail picture of a file and provides options like share or
 * delete.
 *
 * @param {*} props
 * @return {object} vdom of the component
 */
function ImageComp (props) {
  const [src, setSrc] = useState()
  useEffect(() => {
    if (props.file.thumb) {
      const dataUrl = localStorage.getItem(props.file.id)
      if (dataUrl) {
        setSrc(dataUrl)
      } else {
        fetch('/files/' + props.file.id + '/thumb.json')
          .then(res => res.json())
          .then(res => execute({ function: 'decryptToString', key: localStorage.getItem('pk'), data: res }))
          .then(res => {
            setSrc(res.plain)
            if (!localStorage.getItem(props.file.id)) {
              localStorage.setItem(props.file.id, res.plain)
            }
          })
      }
    }
  }, [true])
  const selectElement = e => {
    e.preventDefault()
    props.select()
  }
  let style = {}
  if (props.isSelected) {
    style = { backgroundColor: '#d1ecf7' }
  }
  return h('div', {
    style: style,
    onClick: props.isSelection ? selectElement : () => download(props.file),
    oncontextmenu: selectElement,
    className: 'file'
  },
  src && h('img', { src: src, alt: props.file.name }),
  props.file.name
  )
}
export { Folder }
