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

async function encryptAES (input) {
  await init()
  return wasm.encryptAES(input)
}
async function decryptAES (data, password) {
  await init()
  return wasm.encryptAES(data, password)
}
export { encryptAES, decryptAES }
