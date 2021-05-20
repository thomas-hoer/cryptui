'use strict'

const functions = {}
async function init () {
  const go = new Go()
  return WebAssembly.instantiateStreaming(
    fetch('/wasm/main.wasm'),
    go.importObject
  ).then(result => {
    go.run(result.instance)
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
  if (functions.decryptToString) {
    return functions.decryptToString(input)
  }
  return init().then(() => functions.decryptToString(input))
}
async function decryptToBase64 (input) {
  if (functions.decryptToBase64) {
    return functions.decryptToBase64(input)
  }
  return init().then(() => functions.decryptToBase64(input))
}
async function encryptString (input) {
  if (functions.encryptString) {
    return functions.encryptString(input)
  }
  return init().then(() => functions.encryptString(input))
}
async function encrypt (input) {
  if (functions.encrypt) {
    return functions.encrypt(input)
  }
  return init().then(() => functions.encrypt(input))
}
async function createKey () {
  if (functions.createKey) {
    return functions.createKey()
  }
  return init().then(functions.createKey)
}
async function encryptAES (input) {
  if (functions.encryptAES) {
    return functions.encryptAES(input)
  }
  return init().then(() => functions.encryptAES(input))
}
async function decryptAES (data, password) {
  if (functions.encryptAES) {
    return functions.encryptAES(data, password)
  }
  return init().then(() => functions.encryptAES(data, password))
}
async function signFile (input) {
  if (functions.signFile) {
    return functions.signFile(input)
  }
  return init().then(() => functions.signFile(input))
}
export { decryptToString, decryptToBase64, encryptString, encrypt, encryptAES, decryptAES, signFile, createKey }
