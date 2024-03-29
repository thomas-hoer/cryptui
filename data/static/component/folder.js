'use strict'
import { h, Fragment } from '/js/preact.js'
import { useState, useEffect, useRef } from '/js/hooks.js'
import { execute } from '/js/wasm.js'
import { Board, Grid } from '/component/components.js'

function signAndSend (body, method, location, contentType, etag) {
  const data = {
    function: 'sign',
    key: localStorage.getItem('pk'),
    data: body
  }
  return execute(data).then(out => {
    const headers = {
      'User-Id': localStorage.getItem('userId'),
      signature: out.sign
    }
    if (contentType) {
      headers['Content-Type'] = contentType
    }
    if (etag && etag.current) {
      headers['If-Match'] = etag.current
    }
    return fetch(location, {
      method: method,
      body: out.data,
      headers: headers
    })
  })
}

const download = f => {
  fetch('/files/' + f.id + '/data.json')
    .then(res => res.json())
    .then(res => execute({ function: 'decryptToBase64', key: localStorage.getItem('pk'), data: res }))
    .then(enc => {
      const a = document.createElement('a')
      a.noRouter = true // needed for router.js
      a.href = 'data:octet/stream;base64,' + enc.base64
      a.download = f.name
      a.click()
    })
}

const toBlob = file => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsArrayBuffer(file)
    reader.onload = () => resolve(reader.result)
    reader.onerror = (error) => reject(error)
  })
}

const upload = async file => {
  const result = await toBlob(file)
  const int8Array = new Uint8Array(result)
  return execute({ function: 'encryptArray', key: localStorage.getItem('pk'), plain: int8Array }).then(enc => {
    const body = JSON.stringify({ data: enc.data, key: enc.datakey, nonce: enc.nonce })
    return signAndSend(body, 'POST', '/files/', 'application/file.instance')
  })
    .then((res) => {
      return { id: res.headers.get('Id'), file: file }
    })
}

const uploadFiles = (ev, afterUpload, addUpload) => {
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
      await upload(file).then(e => {
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
        execute({ function: 'encryptString', key: localStorage.getItem('pk'), plain: dataUrl })
          .then(enc => {
            const body = JSON.stringify({ data: enc.data, key: enc.datakey, nonce: enc.nonce })
            signAndSend(body, 'PUT', '/files/' + id + '/thumb.json', 'application/json').then(resolve)
          })
      })
    }
  })
}

function loadAndDecrypt (name, etag, then) {
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
  return fetch(name)
    .then(res => {
      etag.current = res.headers.get('ETag')
      return res.json()
    })
    .then(res => execute({ function: 'decryptToString', key: localStorage.getItem('pk'), data: res }))
    .then(out => then(JSON.parse(out.plain)))
}
function saveLocally (name, data) {
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
  const etag = useRef()
  const filesRef = useRef([])
  const knownFiles = filesRef.current
  const [files, setFiles] = useState(knownFiles)
  const [newFolderName, setNewFolderName] = useState('')
  const [folders, setFolders] = useState([])
  const uploadListRef = useRef([])
  const [uploadList, setUploadList] = useState([])
  const [selected, setSelected] = useState({})

  const loadFilesFromServer = () => loadAndDecrypt('files', etag, json => {
    knownFiles.length = 0
    knownFiles.push(...json)
    setFiles([...knownFiles])
  })

  useEffect(() => {
    loadFilesFromServer()
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
  const addFile = async f => {
    knownFiles.push(f)
    const enc = await execute({ function: 'encryptString', key: localStorage.getItem('pk'), plain: JSON.stringify(knownFiles) })
    const body = JSON.stringify({ data: enc.data, key: enc.datakey, nonce: enc.nonce })
    signAndSend(body, 'PUT', 'files', 'application/json', etag)
      .then(res => {
        if (res.status === 200) {
          etag.current = res.headers.get('ETag')
        } else if (res.status === 412) {
          loadFilesFromServer().then(() => addFile(f))
        }
      })
    saveLocally('files', [...knownFiles])
    setFiles([...knownFiles])
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
      filesRef.current = filesRef.current.filter(f => !selected[f.id])
      const enc = await execute({ function: 'encryptString', key: localStorage.getItem('pk'), plain: JSON.stringify(filesRef.current) })
      const body = JSON.stringify({ data: enc.data, key: enc.datakey, nonce: enc.nonce })
      signAndSend(body, 'PUT', 'files', 'application/json', etag).then(res => { etag.current = res.headers.get('ETag') })
      saveLocally('files', [...filesRef.current])
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
        h('form', { onsubmit: (ev) => uploadFiles(ev, afterUpload, addUpload) },
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
          if (selected[f.id]) {
            delete selected[f.id]
            if (Object.keys(selected).length === 0) {
              props.setMenu(undefined)
            }
          } else {
            selected[f.id] = true
            props.setMenu(menu)
          }
          setSelected({ ...selected })
        },
        isSelected: selected[f.id],
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
