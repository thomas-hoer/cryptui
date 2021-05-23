'use strict'

const functions = {}
async function init () {
  if (functions.init) {
    return
  }
  const go = new Go()
  await WebAssembly.instantiateStreaming(
    fetch('/wasm/main.wasm'),
    go.importObject
  ).then(result => {
    go.run(result.instance)
    functions.init = true
  })
}

async function decryptToBase64 (input) {
  await init()
  return wasm.decryptToBase64(input)
}
async function encryptString (input) {
  await init()
  return wasm.encryptString(input)
}
async function createKey () {
  await init()
  return wasm.createKey()
}
async function encryptAES (input) {
  await init()
  return wasm.encryptAES(input)
}
async function decryptAES (data, password) {
  await init()
  return wasm.encryptAES(data, password)
}
export { decryptToBase64, encryptString, encryptAES, decryptAES, createKey }
