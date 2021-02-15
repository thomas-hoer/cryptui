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
	<-c
}

func createKey(this js.Value, args []js.Value) interface{} {
	getRsaKey()
	return nil
}
func encryptString(this js.Value, args []js.Value) interface{} {
	rng := rand.Reader
	label := []byte("orders")

	secretMessage := []byte(args[0].String())
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
func encrypt(this js.Value, args []js.Value) interface{} {
	rng := rand.Reader
	label := []byte("orders")

	input := args[0]
	size := input.Length()
	secretMessage := make([]byte, size)
	js.CopyBytesToGo(secretMessage, input)
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
func decryptToString(this js.Value, args []js.Value) interface{} {
	rng := rand.Reader
	label := []byte("orders")
	input := args[0]
	ciphertext, _ := base64.StdEncoding.DecodeString(input.Get("data").String())
	ciphertextKey, _ := base64.StdEncoding.DecodeString(input.Get("key").String())
	nonce, _ := base64.StdEncoding.DecodeString(input.Get("nonce").String())
	rsaKey := getRsaKey()
	aesKey, _ := rsa.DecryptOAEP(sha256.New(), rng, rsaKey, ciphertextKey, label)
	block, _ := aes.NewCipher(aesKey)
	aesGCM, _ := cipher.NewGCM(block)
	plain, _ := aesGCM.Open(nil, nonce, ciphertext, nil)
	return string(plain)
}

func decryptToBase64(this js.Value, args []js.Value) interface{} {
	rng := rand.Reader
	label := []byte("orders")
	input := args[0]
	ciphertext, _ := base64.StdEncoding.DecodeString(input.Get("data").String())
	ciphertextKey, _ := base64.StdEncoding.DecodeString(input.Get("key").String())
	nonce, _ := base64.StdEncoding.DecodeString(input.Get("nonce").String())
	rsaKey := getRsaKey()
	aesKey, _ := rsa.DecryptOAEP(sha256.New(), rng, rsaKey, ciphertextKey, label)
	block, _ := aes.NewCipher(aesKey)
	aesGCM, _ := cipher.NewGCM(block)
	plain, _ := aesGCM.Open(nil, nonce, ciphertext, nil)
	return base64.StdEncoding.EncodeToString(plain)
}

func getRsaKey() *rsa.PrivateKey {
	key := Retrieve("pk")
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
		Store("pub", pubBase64)
		Store("pk", pkBase64)
		return rsaKey
	}
}
func newKey() []byte {
	key, _ := rsa.GenerateKey(rand.Reader, 2048)
	return x509.MarshalPKCS1PrivateKey(key)
}
func Store(key string, val string) {
	js.Global().Get("localStorage").Call("setItem", key, val)
}

func Retrieve(key string) *string {
	val := js.Global().Get("localStorage").Call("getItem", key)
	if val.Truthy() {
		value := val.String()
		return &value
	}
	return nil
}
