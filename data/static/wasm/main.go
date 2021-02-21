package main

import (
	"crypto/aes"
	"crypto/cipher"
	"crypto/rand"
	"crypto/rsa"
	"crypto/sha256"
	"crypto/x509"
	"encoding/base64"
	"syscall/js"
)

func main() {
	c := make(chan bool)
	js.Global().Set("decryptToString", js.FuncOf(decryptToString))
	js.Global().Set("decryptToBase64", js.FuncOf(decryptToBase64))
	js.Global().Set("encryptString", js.FuncOf(encryptString))
	js.Global().Set("encrypt", js.FuncOf(encrypt))
	js.Global().Set("createKey", js.FuncOf(createKey))
	go getRsaKey()
	<-c
}

func createKey(this js.Value, args []js.Value) interface{} {
	getRsaKey()
	return nil
}

func encryptData(secretMessage []byte) map[string]interface{} {
	rng := rand.Reader
	label := []byte("orders")
	rsaKey := getRsaKey()

	aesKey := make([]byte, 16)
	nonce := make([]byte, 12)
	rand.Read(aesKey)
	rand.Read(nonce)
	block, _ := aes.NewCipher(aesKey)
	aesGCM, _ := cipher.NewGCM(block)
	ciphertext := aesGCM.Seal(nil, nonce, secretMessage, nil)

	ciphertextKey, _ := rsa.EncryptOAEP(sha256.New(), rng, &rsaKey.PublicKey, aesKey, label)
	result := make(map[string]interface{})
	result["data"] = base64.StdEncoding.EncodeToString(ciphertext)
	result["key"] = base64.StdEncoding.EncodeToString(ciphertextKey)
	result["nonce"] = base64.StdEncoding.EncodeToString(nonce)

	return result
}

func encryptString(this js.Value, args []js.Value) interface{} {
	secretMessage := []byte(args[0].String())
	return encryptData(secretMessage)
}

func encrypt(this js.Value, args []js.Value) interface{} {
	input := args[0]
	size := input.Length()
	secretMessage := make([]byte, size)
	js.CopyBytesToGo(secretMessage, input)
	return encryptData(secretMessage)
}

func decrypt(input js.Value) []byte {
	rng := rand.Reader
	label := []byte("orders")
	ciphertext, _ := base64.StdEncoding.DecodeString(input.Get("data").String())
	ciphertextKey, _ := base64.StdEncoding.DecodeString(input.Get("key").String())
	nonce, _ := base64.StdEncoding.DecodeString(input.Get("nonce").String())
	rsaKey := getRsaKey()
	aesKey, _ := rsa.DecryptOAEP(sha256.New(), rng, rsaKey, ciphertextKey, label)
	block, _ := aes.NewCipher(aesKey)
	aesGCM, _ := cipher.NewGCM(block)
	plain, _ := aesGCM.Open(nil, nonce, ciphertext, nil)
	return plain
}

func decryptToString(this js.Value, args []js.Value) interface{} {
	return string(decrypt(args[0]))
}

func decryptToBase64(this js.Value, args []js.Value) interface{} {
	return base64.StdEncoding.EncodeToString(decrypt(args[0]))
}

func getRsaKey() *rsa.PrivateKey {
	key := retrieve("pk")
	if key != nil {
		data, _ := base64.StdEncoding.DecodeString(*key)
		pk, _ := x509.ParsePKCS1PrivateKey(data)
		return pk
	} else {
		rsaKey, _ := rsa.GenerateKey(rand.Reader, 2048)
		pkData := x509.MarshalPKCS1PrivateKey(rsaKey)
		pkBase64 := base64.StdEncoding.EncodeToString(pkData)
		pubData := x509.MarshalPKCS1PublicKey(&rsaKey.PublicKey)
		pubBase64 := base64.StdEncoding.EncodeToString(pubData)
		store("pub", pubBase64)
		store("pk", pkBase64)
		return rsaKey
	}
}

func newKey() []byte {
	key, _ := rsa.GenerateKey(rand.Reader, 2048)
	return x509.MarshalPKCS1PrivateKey(key)
}

func store(key string, val string) {
	js.Global().Get("localStorage").Call("setItem", key, val)
}

func retrieve(key string) *string {
	val := js.Global().Get("localStorage").Call("getItem", key)
	if val.Truthy() {
		value := val.String()
		return &value
	}
	return nil
}
