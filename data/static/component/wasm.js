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
    functions.decryptToString = wasmDecryptToString
    functions.decryptToBase64 = wasmDecryptToBase64
    functions.encryptString = wasmEncryptString
    functions.encrypt = wasmEncrypt
    functions.createKey = wasmCreateKey
    functions.encryptAES = wasmEncryptAES
    functions.decryptAES = wasmDecryptAES
    functions.signFile = wasmSignFile
  })
}

async function decryptToString (input) {
  await init()
  return functions.decryptToString(input)
}
async function decryptToBase64 (input) {
  await init()
  return functions.decryptToBase64(input)
}
async function encryptString (input) {
  await init()
  return functions.encryptString(input)
}
async function encrypt (input) {
  await init()
  return functions.encrypt(input)
}
async function createKey () {
  await init()
  return functions.createKey()
}
async function encryptAES (input) {
  await init()
  return functions.encryptAES(input)
}
async function decryptAES (data, password) {
  await init()
  return functions.encryptAES(data, password)
}
async function signFile (input) {
  await init()
  return functions.signFile(input)
}
export { decryptToString, decryptToBase64, encryptString, encrypt, encryptAES, decryptAES, signFile, createKey }
