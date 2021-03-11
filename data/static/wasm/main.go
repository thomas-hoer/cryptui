// +build js,wasm

package main

import (
	"crypto"
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
	js.Global().Set("decryptToString", js.FuncOf(jsDecryptToString))
	js.Global().Set("decryptToBase64", js.FuncOf(jsDecryptToBase64))
	js.Global().Set("encryptString", js.FuncOf(jsEncryptString))
	js.Global().Set("encrypt", js.FuncOf(jsEncrypt))
	js.Global().Set("createKey", js.FuncOf(jsCreateKey))
	js.Global().Set("encryptAES", js.FuncOf(jsEncryptAES))
	js.Global().Set("decryptAES", js.FuncOf(jsDecryptAES))
	js.Global().Set("signFile", js.FuncOf(jsSign))
	<-c
}

func jsSign(this js.Value, args []js.Value) interface{} {
	rng := rand.Reader
	input := args[0].String()

	rsaPrivateKey := getRsaKey()
	hashed := sha256.Sum256([]byte(input))
	signature, _ := rsa.SignPKCS1v15(rng, rsaPrivateKey, crypto.SHA256, hashed[:])
	return base64.StdEncoding.EncodeToString(signature)
}
func jsCreateKey(this js.Value, args []js.Value) interface{} {
	getRsaKey()
	return nil
}

func jsEncryptAES(this js.Value, args []js.Value) interface{} {
	secretMessage := args[0].String()
	password := args[1].String()
	return encryptAES(secretMessage, password)
}

func jsDecryptAES(this js.Value, args []js.Value) interface{} {
	secretMessage := args[0]
	password := args[1].String()
	return decryptAES(secretMessage, password)
}
func decryptAES(input js.Value, password string) string {
	ciphertext, _ := base64.StdEncoding.DecodeString(input.Get("data").String())
	salt, _ := base64.StdEncoding.DecodeString(input.Get("salt").String())
	nonce, _ := base64.StdEncoding.DecodeString(input.Get("nonce").String())
	sha := sha256.New()
	sha.Write(salt)
	aesKey := sha.Sum([]byte(password))[:16]

	block, _ := aes.NewCipher(aesKey)
	aesGCM, _ := cipher.NewGCM(block)
	plain, _ := aesGCM.Open(nil, nonce, ciphertext, nil)
	return string(plain)
}
func encryptAES(secretMessage string, password string) map[string]interface{} {
	nonce := make([]byte, 12)
	rand.Read(nonce)
	salt := make([]byte, 12)
	rand.Read(salt)

	sha := sha256.New()
	sha.Write(salt)
	aesKey := sha.Sum([]byte(password))[:16]
	block, _ := aes.NewCipher(aesKey)
	aesGCM, _ := cipher.NewGCM(block)
	ciphertext := aesGCM.Seal(nil, nonce, []byte(secretMessage), nil)
	result := make(map[string]interface{})

	result["data"] = base64.StdEncoding.EncodeToString(ciphertext)
	result["nonce"] = base64.StdEncoding.EncodeToString(nonce)
	result["salt"] = base64.StdEncoding.EncodeToString(salt)

	return result
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

func jsEncryptString(this js.Value, args []js.Value) interface{} {
	secretMessage := []byte(args[0].String())
	return encryptData(secretMessage)
}

func jsEncrypt(this js.Value, args []js.Value) interface{} {
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
	aesKey, err := rsa.DecryptOAEP(sha256.New(), rng, rsaKey, ciphertextKey, label)
	if err != nil {
		return nil
	}
	block, _ := aes.NewCipher(aesKey)
	aesGCM, err := cipher.NewGCM(block)
	if err != nil {
		return nil
	}
	plain, err := aesGCM.Open(nil, nonce, ciphertext, nil)
	if err != nil {
		return nil
	}
	return plain
}

func jsDecryptToString(this js.Value, args []js.Value) interface{} {
	return string(decrypt(args[0]))
}

func jsDecryptToBase64(this js.Value, args []js.Value) interface{} {
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
