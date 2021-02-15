'use strict'

const toBase64 = file => new Promise((resolve, reject) => {
	const reader = new FileReader()
	reader.readAsArrayBuffer(file)
	reader.onload = () => resolve(reader.result)
	reader.onerror = error => reject(error)
})

export {toBase64}