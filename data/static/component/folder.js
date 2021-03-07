'use strict'
import { h, Fragment } from '/js/preact.js'
import { useState, useEffect, useRef } from '/js/hooks.js'
import { Board } from '/component/board.js'
import { Grid } from '/component/grid.js'

const download = (f) => {
  fetch('/files/' + f.id + '/data.json').then((res) => res.json()).then((res) => {
    const str = decryptToBase64(res)
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

const upload = async (file) => {
  const result = await toBlob(file)
  const int8Array = new Uint8Array(result)
  const enc = encrypt(int8Array)
  return fetch('/files/', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/file.instance'
    },
    body: JSON.stringify(enc)
  }).then((res) => {
    return { id: res.headers.get('Id'), file: file }
  })
}

const uploadFiles = (ev, afterUpload) => {
  ev.preventDefault()
  const files = ev.target[0].files
  for (let i = 0; i < files.length; i++) {
    upload(files[i]).then(afterUpload)
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
        ctx.drawImage(image, 0, 0, 300, 150)
        const thumbnail = encryptString(canvas.toDataURL('image/jpeg', 0.2))
        fetch('/files/' + id + '/thumb.json', {
          method: 'PUT',
          body: JSON.stringify(thumbnail)
        }).then(resolve)
      })
    }
  })
}

/**
 * Creates a component that lists all files in the current directory relative
 * to the window state.
 *
 * @return {object} vdom of the component
 */
function Folder () {
  const filesRef = useRef([])
  const knownFiles = filesRef.current
  const [files, setFiles] = useState(knownFiles)
  const [newFolderName, setNewFolderName] = useState('')
  const [folders, setFolders] = useState([])
  useEffect(() => {
    fetch('files').then((res) => res.json()).then((res) => {
      const json = decryptToString(res)
      knownFiles.push(...JSON.parse(json))
      setFiles([...knownFiles])
    })
    fetch('?json').then((res) => res.json()).then(setFolders)
  }, [true])

  const addFile = (f) => {
    knownFiles.push(f)
    const enc = encryptString(JSON.stringify(knownFiles))
    fetch('files', {
      method: 'PUT',
      body: JSON.stringify(enc)
    })
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
  const addFolder = (ev) => {
    ev.preventDefault()
    fetch(folder + '/type', {
      method: 'PUT',
      body: 'folder/instance'
    }).then(() => {
      setFolders([...folders, folder + '/'])
      setFolder('')
    })
  }
  return h(Fragment, null,
    h(Grid, null,
      h(Board, { title: 'Upload' },
        h('form', { onsubmit: (ev) => uploadFiles(ev, afterUpload) },
          h('input', { type: 'file', multiple: 'multiple' }),
          h('input', { type: 'submit' })
        )
      ),
      h(Board, { title: 'New Folder' },
        h('form', { onsubmit: addFolder },
          h('input', {
            type: 'text',
            value: newFolderName,
            onChange: (ev) => setNewFolderName(ev.target.value)
          }),
          h('input', { type: 'submit' })
        )
      )
    ),
    h(Grid, null,
      folders
        .filter((f) => f.includes('/'))
        .map((f) => h('a', {
          href: f,
          className: 'folder'
        }, f.replace('/', '')))
    ),
    h(Grid, null,
      files.map((f) => h(ImageComp, { file: f }))
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
      fetch('/files/' + props.file.id + '/thumb.json')
        .then((res) => res.json())
        .then((res) => setSrc(decryptToString(res)))
    }
  }, [true])
  return h('div', { onClick: () => download(props.file), className: 'file' },
    src && h('img', { src: src }),
    props.file.name
  )
}
export { Folder }
